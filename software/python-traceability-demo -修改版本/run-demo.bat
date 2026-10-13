@echo off
setlocal EnableExtensions
cd /d "%~dp0"

set "PYTHON_CMD="
where py >nul 2>nul
if not errorlevel 1 set "PYTHON_CMD=py -3"
if not defined PYTHON_CMD (
    where python >nul 2>nul
    if not errorlevel 1 set "PYTHON_CMD=python"
)

if not defined PYTHON_CMD (
    echo [ERROR] Python not found. Please install Python 3.10 or newer.
    pause
    exit /b 1
)

set "TRACE_PORT=8000"
if exist "server-port.txt" set /p TRACE_PORT=<"server-port.txt"

call :check_our_service %TRACE_PORT%
if not errorlevel 1 (
    echo [INFO] Modified traceability service is already running on port %TRACE_PORT%.
    call %PYTHON_CMD% server.py --refresh-qr --port %TRACE_PORT%
    echo [INFO] QR image was refreshed with the current LAN IP and port.
    start "" "%CD%\trace-qr.png"
    pause
    exit /b 0
)

for /f %%P in ('powershell -NoProfile -Command "$p=8000; while(Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue){$p++}; $p"') do set "TRACE_PORT=%%P"

echo [INFO] Starting modified traceability service on port %TRACE_PORT%...
echo [INFO] If port 8000 belongs to another or stale service, a new free port is used.
set "TRACE_SHOW_QR=1"
call %PYTHON_CMD% server.py --port %TRACE_PORT%
exit /b %errorlevel%

:check_our_service
powershell -NoProfile -Command "try {$h=Invoke-RestMethod -Uri 'http://127.0.0.1:%~1/api/health' -TimeoutSec 2; if($h.mode -eq 'main-system-data' -and $h.instance_dir -eq [IO.Path]::GetFullPath('%CD%')){exit 0}} catch {}; exit 1" >nul 2>nul
exit /b %errorlevel%
