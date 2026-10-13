@echo off
setlocal
cd /d "%~dp0"

netstat -ano | findstr /C:":8000" | findstr "LISTENING" >nul 2>nul
if not errorlevel 1 (
    echo [INFO] Traceability service is already running.
    echo [INFO] Open http://127.0.0.1:8000 in your browser.
    pause
    exit /b 0
)

where py >nul 2>nul
if not errorlevel 1 (
    py -3 server.py
    exit /b
)

where python >nul 2>nul
if not errorlevel 1 (
    python server.py
    exit /b
)

echo [ERROR] Python not found. Please install Python 3.10 or newer.
pause
