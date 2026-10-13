param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("start-mysql", "start-redis", "stop-mysql", "stop-redis")]
    [string]$Action,
    [string]$Root = "",
    [int]$MySqlPort = 3306,
    [int]$RedisPort = 6379,
    [string]$MySqlPassword = "root"
)

$ErrorActionPreference = "Continue"

if ([string]::IsNullOrWhiteSpace($Root)) {
    $Root = $PSScriptRoot + "\.."
}
$Root = [IO.Path]::GetFullPath($Root)
if (-not $Root.EndsWith("\")) {
    $Root += "\"
}

$runtime = Join-Path $Root "runtime"
$mysqlDir = Join-Path $runtime "mysql"
$mysqlData = Join-Path $runtime "mysql-data"
$redisDir = Join-Path $runtime "redis"
$envMysql = Join-Path $Root "env\mysql"
$envRedis = Join-Path $Root "env\redis"
$mysqlBin = Join-Path $envMysql "bin"
$mysqldExe = Join-Path $mysqlBin "mysqld.exe"
$mysqlExe = Join-Path $mysqlBin "mysql.exe"
$mysqladminExe = Join-Path $mysqlBin "mysqladmin.exe"
$mysqlIni = Join-Path $mysqlDir "my.ini"
$mysqlPidFile = Join-Path $mysqlDir "mysql.pid"
$mysqlOutLog = Join-Path $mysqlDir "mysql.out.log"
$mysqlErrLog = Join-Path $mysqlDir "mysql.err.log"
$redisExe = Join-Path $envRedis "redis-server.exe"
$redisCli = Join-Path $envRedis "redis-cli.exe"
$redisConf = Join-Path $redisDir "redis.conf"
$redisPidFile = Join-Path $redisDir "redis.pid"
$redisOutLog = Join-Path $redisDir "redis.out.log"
$redisErrLog = Join-Path $redisDir "redis.err.log"

function New-Directory([string]$Path) {
    if (-not (Test-Path -LiteralPath $Path)) {
        New-Item -ItemType Directory -Path $Path -Force | Out-Null
    }
}

function Test-Port([int]$Port) {
    $client = New-Object System.Net.Sockets.TcpClient
    try {
        $client.Connect("127.0.0.1", $Port)
        return $true
    } catch {
        return $false
    } finally {
        $client.Dispose()
    }
}

function Wait-Port([int]$Port, [int]$Seconds) {
    for ($i = 0; $i -lt $Seconds; $i++) {
        if (Test-Port $Port) {
            return $true
        }
        Start-Sleep -Seconds 1
    }
    return $false
}

function Wait-NoPort([int]$Port, [int]$Seconds) {
    for ($i = 0; $i -lt $Seconds; $i++) {
        if (-not (Test-Port $Port)) {
            return $true
        }
        Start-Sleep -Seconds 1
    }
    return $false
}

function Get-PidFromFile([string]$Path) {
    if (Test-Path -LiteralPath $Path) {
        $text = (Get-Content -LiteralPath $Path -Raw).Trim()
        if ($text -match "^\d+$") {
            return [int]$text
        }
    }
    return 0
}

function Test-EnvProcess([int]$Id, [string]$ExePath) {
    if ($Id -le 0) {
        return $false
    }
    $proc = Get-Process -Id $Id -ErrorAction SilentlyContinue
    if (-not $proc) {
        return $false
    }
    try {
        $actualPath = $proc.Path
    } catch {
        return $false
    }
    if ([string]::IsNullOrWhiteSpace($actualPath)) {
        return $false
    }
    $fullExe = [IO.Path]::GetFullPath($ExePath)
    return [string]::Equals($actualPath, $fullExe, [StringComparison]::OrdinalIgnoreCase)
}

function Start-MySql {
    New-Directory $mysqlDir

    if (Test-Port $MySqlPort) {
        Write-Host "  [portable] MySQL already listening on port $MySqlPort. Skipping bundled MySQL."
        return
    }

    if (-not (Test-Path -LiteralPath $mysqldExe)) {
        throw "Portable MySQL executable not found: $mysqldExe"
    }

    if (-not (Test-Path -LiteralPath (Join-Path $mysqlData "mysql"))) {
        Write-Host "  [portable] Initializing portable MySQL data directory..."
        $initArgs = @(
            "--no-defaults",
            "--initialize-insecure",
            "--basedir=`"$envMysql`"",
            "--datadir=`"$mysqlData`""
        )
        $init = Start-Process -FilePath $mysqldExe -ArgumentList $initArgs -WorkingDirectory $mysqlBin -WindowStyle Hidden -Wait -PassThru
        if ($init.ExitCode -ne 0) {
            throw "mysqld --initialize-insecure failed with exit code $($init.ExitCode). Check $mysqlErrLog"
        }
        Write-Host "  [portable] MySQL data initialized."
    }

    $ini = @"
[mysqld]
basedir=$($envMysql.Replace('\','/'))
datadir=$($mysqlData.Replace('\','/'))
port=$MySqlPort
bind-address=127.0.0.1
character-set-server=utf8mb4
collation-server=utf8mb4_0900_ai_ci
lower_case_table_names=1
max_connections=200
mysqlx=0
"@
    $ini | Set-Content -LiteralPath $mysqlIni -Encoding ASCII

    $startArgs = @(
        "--defaults-file=`"$mysqlIni`"",
        "--log-error=`"$($mysqlErrLog.Replace('\','/'))`""
    )
    $proc = Start-Process -FilePath $mysqldExe -ArgumentList $startArgs -WorkingDirectory $mysqlBin -WindowStyle Hidden -PassThru
    if (-not $proc) {
        throw "Failed to start mysqld.exe"
    }
    Set-Content -LiteralPath $mysqlPidFile -Value $proc.Id -Encoding ASCII
    Write-Host "  [portable] Starting portable MySQL on 127.0.0.1:$MySqlPort (PID $($proc.Id))..."

    if (-not (Wait-Port $MySqlPort 90)) {
        if (Test-EnvProcess $proc.Id $mysqldExe) {
            Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
        }
        throw "MySQL did not listen on port $MySqlPort within 90s. Check $mysqlErrLog"
    }

    $freshEmptyPassword = $false
    & $mysqlExe -h 127.0.0.1 -P $MySqlPort -u root --protocol=TCP -N -B -e "SELECT 1;" 2>$null
    if ($LASTEXITCODE -eq 0) {
        $freshEmptyPassword = $true
    } else {
        & $mysqlExe -h 127.0.0.1 -P $MySqlPort -u root "-p$MySqlPassword" --protocol=TCP -N -B -e "SELECT 1;" 2>$null
        if ($LASTEXITCODE -ne 0) {
            throw "Cannot access portable MySQL root account. Check $mysqlErrLog"
        }
        Write-Host "  [portable] MySQL root account already configured."
    }

    if ($freshEmptyPassword) {
        Write-Host "  [portable] Setting MySQL root password..."
        $sql = "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '$MySqlPassword'; CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED WITH mysql_native_password BY '$MySqlPassword'; ALTER USER IF EXISTS 'root'@'%' IDENTIFIED WITH mysql_native_password BY '$MySqlPassword'; GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION; FLUSH PRIVILEGES;"
        & $mysqlExe -h 127.0.0.1 -P $MySqlPort -u root --protocol=TCP -e $sql
        if ($LASTEXITCODE -ne 0) {
            throw "Failed to set portable MySQL root password. Check $mysqlErrLog"
        }
    }
    Write-Host "  [portable] Portable MySQL ready on 127.0.0.1:$MySqlPort (root / $MySqlPassword)."
}

function Start-Redis {
    New-Directory $redisDir

    if (Test-Port $RedisPort) {
        Write-Host "  [portable] Redis already listening on port $RedisPort. Skipping bundled Redis."
        return
    }

    if (-not (Test-Path -LiteralPath $redisExe)) {
        throw "Portable Redis executable not found: $redisExe"
    }

    $conf = @"
port $RedisPort
bind 127.0.0.1
protected-mode yes
daemonize no
logfile $($redisOutLog.Replace('\','/'))
save ""
appendonly no
dir $($redisDir.Replace('\','/'))
"@
    $conf | Set-Content -LiteralPath $redisConf -Encoding ASCII

    $proc = Start-Process -FilePath $redisExe -ArgumentList @("`"$redisConf`"") -WorkingDirectory $redisDir -WindowStyle Hidden -PassThru
    if (-not $proc) {
        throw "Failed to start redis-server.exe"
    }
    Set-Content -LiteralPath $redisPidFile -Value $proc.Id -Encoding ASCII
    Write-Host "  [portable] Starting portable Redis on 127.0.0.1:$RedisPort (PID $($proc.Id))..."

    if (-not (Wait-Port $RedisPort 30)) {
        if (Test-EnvProcess $proc.Id $redisExe) {
            Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
        }
        throw "Redis did not listen on port $RedisPort within 30s. Check $redisErrLog"
    }
    Write-Host "  [portable] Portable Redis ready on 127.0.0.1:$RedisPort."
}

function Stop-MySql {
    if (-not (Test-Port $MySqlPort)) {
        if (Test-Path -LiteralPath $mysqlPidFile) {
            Remove-Item -LiteralPath $mysqlPidFile -Force -ErrorAction SilentlyContinue
        }
        Write-Host "  [portable] MySQL is not running on port $MySqlPort."
        return
    }

    $savedPid = Get-PidFromFile $mysqlPidFile
    if ($savedPid -gt 0 -and (Test-EnvProcess $savedPid $mysqldExe)) {
        Write-Host "  [portable] Shutting down portable MySQL (PID $savedPid)..."
        if (Test-Path -LiteralPath $mysqladminExe) {
            $env:MYSQL_PWD = $MySqlPassword
            & $mysqladminExe -h 127.0.0.1 -P $MySqlPort -u root --protocol=TCP shutdown | Out-Null
            $env:MYSQL_PWD = $null
        }
        if (Wait-NoPort $MySqlPort 30) {
            Write-Host "  [portable] MySQL stopped."
        } else {
            Write-Host "  [portable] mysqladmin shutdown timed out; stopping PID $savedPid."
            Stop-Process -Id $savedPid -Force -ErrorAction SilentlyContinue
            Wait-NoPort $MySqlPort 15 | Out-Null
        }
        Remove-Item -LiteralPath $mysqlPidFile -Force -ErrorAction SilentlyContinue
    } else {
        Write-Host "  [portable] No bundled MySQL PID found for port $MySqlPort. Not stopping system MySQL."
    }
}

function Stop-Redis {
    if (-not (Test-Port $RedisPort)) {
        if (Test-Path -LiteralPath $redisPidFile) {
            Remove-Item -LiteralPath $redisPidFile -Force -ErrorAction SilentlyContinue
        }
        Write-Host "  [portable] Redis is not running on port $RedisPort."
        return
    }

    $savedPid = Get-PidFromFile $redisPidFile
    if ($savedPid -gt 0 -and (Test-EnvProcess $savedPid $redisExe)) {
        Write-Host "  [portable] Shutting down portable Redis (PID $savedPid)..."
        if (Test-Path -LiteralPath $redisCli) {
            & $redisCli -h 127.0.0.1 -p $RedisPort shutdown nosave | Out-Null
        }
        if (Wait-NoPort $RedisPort 15) {
            Write-Host "  [portable] Redis stopped."
        } else {
            Write-Host "  [portable] redis-cli shutdown timed out; stopping PID $savedPid."
            Stop-Process -Id $savedPid -Force -ErrorAction SilentlyContinue
            Wait-NoPort $RedisPort 10 | Out-Null
        }
        Remove-Item -LiteralPath $redisPidFile -Force -ErrorAction SilentlyContinue
    } else {
        Write-Host "  [portable] No bundled Redis PID found for port $RedisPort. Not stopping system Redis."
    }
}

switch ($Action) {
    "start-mysql" { Start-MySql }
    "start-redis" { Start-Redis }
    "stop-mysql" { Stop-MySql }
    "stop-redis" { Stop-Redis }
}
