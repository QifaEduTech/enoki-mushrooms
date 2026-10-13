@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"

set "BACKEND_PORT=8080"
set "BACKEND_DIR=%~dp0Smart-Enokimushroom"
set "FRONTEND_DIST=%~dp0Smart-Enokimushroom-front\dist"
set "FRONTEND_WWW="
for /f "usebackq delims=" %%W in (`powershell -NoProfile -Command "$p=(Get-Process nginx -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty Path); if($p){$base=Split-Path (Split-Path (Split-Path $p -Parent) -Parent) -Parent; Join-Path $base 'WWW'}"`) do set "FRONTEND_WWW=%%W"
set "TRACE_DIR="
for /d %%D in ("%~dp0python-traceability-demo -*") do set "TRACE_DIR=%%~fD"

title Smart-Enokimushroom DEV CONTROL PANEL

echo  ================================================================
echo      Smart-Enokimushroom DEV CONTROL PANEL
echo  ================================================================
echo   SERVICE      PORT   DESCRIPTION
echo   ----------------------------------------------------------------
echo   1. MySQL      3306   database enokimushroom
echo   2. Redis      6379   cache (Memurai)
echo   3. Nginx      80     frontend panel (phpstudy)
echo   4. Backend    8080   Spring Boot (built from source)
echo  ================================================================
echo.

rem ---- 0. Traceability service ----
if exist "%TRACE_DIR%\server.py" (
  powershell -NoProfile -Command "$ok=$false; 8000..8010 | ForEach-Object {try {$h=Invoke-RestMethod -Uri ('http://127.0.0.1:'+$_+'/api/health') -TimeoutSec 1; if($h.mode -eq 'main-system-data'){$ok=$true}} catch {}}; if($ok){exit 0}else{exit 1}" >nul 2>nul
  if errorlevel 1 (
    for /f %%P in ('powershell -NoProfile -Command "$p=8000; while(Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue){$p++}; $p"') do set "TRACE_PORT=%%P"
    where py >nul 2>nul
    if not errorlevel 1 (
      echo   [..] Starting traceability service on :!TRACE_PORT!
      start "Smart-Enokimushroom-Traceability" /d "%TRACE_DIR%" cmd /c "set TRACE_SHOW_QR=0&& py -3 server.py --port !TRACE_PORT! > server.out.log 2> server.err.log"
    ) else (
      where python >nul 2>nul
      if not errorlevel 1 start "Smart-Enokimushroom-Traceability" /d "%TRACE_DIR%" cmd /c "set TRACE_SHOW_QR=0&& python server.py --port !TRACE_PORT! > server.out.log 2> server.err.log"
    )
  ) else (
    echo   [OK] Traceability service running
  )
)

rem ---- 1. MySQL 3306 ----
netstat -ano | findstr ":3306 " | findstr "LISTENING" >nul
if errorlevel 1 (
  echo   [--] MySQL  port 3306  NOT RUNNING
) else (
  echo   [OK] MySQL  port 3306  running
)

rem ---- 2. Redis 6379 ----
netstat -ano | findstr ":6379 " | findstr "LISTENING" >nul
if errorlevel 1 (
  echo   [--] Redis  port 6379  NOT RUNNING
) else (
  echo   [OK] Redis  port 6379  running
)

rem ---- 3. Nginx 80 ----
netstat -ano | findstr ":80 " | findstr "LISTENING" >nul
if errorlevel 1 (
  echo   [--] Nginx  port 80    NOT RUNNING
) else (
  echo   [OK] Nginx  port 80    running
)

rem ---- 4. Backend 8080 ----
netstat -ano | findstr ":%BACKEND_PORT% " | findstr "LISTENING" >nul
if errorlevel 1 (
  echo   [--] Backend port %BACKEND_PORT%  NOT RUNNING
) else (
  echo   [OK] Backend port %BACKEND_PORT%  running
)
echo.

rem ---- start backend if not running ----
netstat -ano | findstr ":%BACKEND_PORT% " | findstr "LISTENING" >nul
if not errorlevel 1 (
  echo  Backend already running on :%BACKEND_PORT%.
  goto openpanel
)

echo  Starting backend from source (first run compiles) ...
start "Smart-Enokimushroom-Backend (:8080)" /d "%BACKEND_DIR%" cmd /k "mvn install -DskipTests -q && cd Enokimushroom-admin && mvn spring-boot:run"

echo  Waiting for backend :%BACKEND_PORT% ...
set /a waited=0
:waitloop
netstat -ano | findstr ":%BACKEND_PORT% " | findstr "LISTENING" >nul
if not errorlevel 1 goto up
timeout /t 2 /nobreak >nul
set /a waited+=2
if %waited% geq 90 (
  echo  [TIMEOUT] Backend not up after 90s. See backend window.
  pause
  exit /b 1
)
goto waitloop

:up
echo  [OK] Backend is UP on :%BACKEND_PORT%

:openpanel
echo.
if defined FRONTEND_WWW if exist "%FRONTEND_DIST%\index.html" if exist "%FRONTEND_WWW%" (
  echo   Publishing latest frontend files ...
  xcopy "%FRONTEND_DIST%\*" "%FRONTEND_WWW%\" /E /I /Y /Q >nul
  if errorlevel 1 (
    echo   [WARN] Frontend publish failed. Run this launcher as administrator.
  ) else (
    echo   [OK] Latest frontend published.
  )
)
echo  ----------------------------------------------------------------
echo   Open frontend panel in browser ...
start "" "http://localhost"
echo.
echo  ----------------------------------------------------------------
echo   ACCESS URLs:
echo     Frontend : http://localhost          (Nginx :80)
echo     Backend  : http://localhost:%BACKEND_PORT%    (Spring Boot)
echo     MySQL    : localhost:3306   db=enokimushroom user=root
echo     Redis    : localhost:6379
echo  ----------------------------------------------------------------
echo   STOP backend: run stop-dev.bat
echo.
echo   Press any key to close this panel (backend keeps running).
pause >nul
exit /b 0
