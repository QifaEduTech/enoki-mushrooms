param(
    [int]$BasePort = 80,
    [int]$MaxPort = 65535
)

$start = [Math]::Max(1, $BasePort)
$end = [Math]::Min(65535, [Math]::Max($start, $MaxPort))

for ($port = $start; $port -le $end; $port++) {
    $listener = New-Object System.Net.Sockets.TcpListener([System.Net.IPAddress]::Any, $port)
    try {
        $listener.Start()
        $listener.Stop()
        Write-Output $port
        exit 0
    } catch {
        try { $listener.Stop() } catch {}
    }
}

Write-Error "No free TCP port found starting from $BasePort."
exit 1
