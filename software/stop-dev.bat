@echo off
setlocal EnableDelayedExpansion

echo ============================================
echo   Smart-Enokimushroom DEV STOPPER
echo ============================================
echo.

rem ---- Stop backend (kill process on port 8080) ----
echo [1/2] Stopping backend on port 8080 ...
set "PID="
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080 " ^| findstr "LISTENING"') do set "PID=%%a"
if defined PID (
  taskkill /PID %PID% /F >nul 2>&1
  echo [OK] Killed process PID %PID% (backend)
) else (
  echo [INFO] Backend not running on 8080.
)

echo.
echo ============================================
echo  Done! Backend stopped.
echo  (phpstudy / frontend on :80 is not touched)
echo ============================================
echo.
pause >nul
