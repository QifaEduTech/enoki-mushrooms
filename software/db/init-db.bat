@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"

title Smart-Enokimushroom Database Loader

set "SQL_FILE=%~dp0enokimushroom.sql"
set "SUPPLEMENT_SQL_FILE=%~dp0trace_supplement.sql"
set "RECENT_DATA_SQL_FILE=%~dp0enoki_recent_data.sql"
set "DB_NAME=enokimushroom"
set "DB_PORT=3306"
set "DB_HOST=127.0.0.1"
set "DB_USER=root"

if defined DB_PWD set "DB_PWD_SAFE=!DB_PWD!"
if not defined DB_PWD_SAFE set "DB_PWD_SAFE=root"

echo  ================================================================
echo      Smart-Enokimushroom DATABASE LOADER
echo  ================================================================
echo   SQL file : %SQL_FILE%
echo   Target   : %DB_HOST%:%DB_PORT% / %DB_NAME%
echo  ================================================================
echo.

set "MYSQL_EXE="
if defined MYSQL_HOME if exist "%MYSQL_HOME%\bin\mysql.exe" set "MYSQL_EXE=%MYSQL_HOME%\bin\mysql.exe"
if not defined MYSQL_EXE where mysql >nul 2>nul
if not defined MYSQL_EXE if not errorlevel 1 set "MYSQL_EXE=mysql"
if not defined MYSQL_EXE if exist "D:\epam\huanjing2\mysql-8.0.46-winx64\bin\mysql.exe" set "MYSQL_EXE=D:\epam\huanjing2\mysql-8.0.46-winx64\bin\mysql.exe"
if not defined MYSQL_EXE if exist "%~dp0..\portable\Smart-Enokimushroom\env\mysql\bin\mysql.exe" set "MYSQL_EXE=%~dp0..\portable\Smart-Enokimushroom\env\mysql\bin\mysql.exe"
if not defined MYSQL_EXE set "MYSQL_EXE=mysql"

echo  [OK] mysql.exe : %MYSQL_EXE%
echo.

echo  Waiting for MySQL on %DB_HOST%:%DB_PORT% ...
set /a waited=0
:waitloop
netstat -ano | findstr ":%DB_PORT% " | findstr "LISTENING" >nul
if not errorlevel 1 goto mysqlup
ping -n 3 127.0.0.1 >nul
set /a waited+=2
if %waited% geq 30 goto mysqltimeout
goto waitloop
:mysqltimeout
echo  [ERROR] MySQL not listening on %DB_PORT% after 30s.
echo          Start MySQL service first, then re-run this script.
pause
exit /b 1
:mysqlup
echo  [OK] MySQL is listening on :%DB_PORT%
echo.

set "MYSQL_PWD=%DB_PWD_SAFE%"

"%MYSQL_EXE%" -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -N -e "SELECT SCHEMA_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME='%DB_NAME%';" >nul 2>"%~dp0db_conn.err"
set "CONN_RC=%errorlevel%"
if not "%CONN_RC%"=="0" goto connfail

"%MYSQL_EXE%" -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='%DB_NAME%';" > "%~dp0db_count.tmp" 2>nul
set /p TABLE_COUNT=<"%~dp0db_count.tmp"
del "%~dp0db_count.tmp" >nul 2>nul
set "TABLE_COUNT=%TABLE_COUNT: =%"

if defined TABLE_COUNT if not "%TABLE_COUNT%"=="0" goto exist

echo  Database "%DB_NAME%" missing or empty. Creating ...
"%MYSQL_EXE%" -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -e "CREATE DATABASE IF NOT EXISTS %DB_NAME% DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
if errorlevel 1 goto createfail
echo  [OK] Database created.

echo  Importing %SQL_FILE% ...
"%MYSQL_EXE%" -h %DB_HOST% -P %DB_PORT% -u %DB_USER% --default-character-set=utf8mb4 %DB_NAME% < "%SQL_FILE%"
if errorlevel 1 goto importfail
echo  [OK] Import finished.

if exist "%SUPPLEMENT_SQL_FILE%" (
    echo  Importing supplement table (packaging/sales/credit) ...
    "%MYSQL_EXE%" -h %DB_HOST% -P %DB_PORT% -u %DB_USER% --default-character-set=utf8mb4 %DB_NAME% < "%SUPPLEMENT_SQL_FILE%"
    if errorlevel 1 goto importfail
    echo  [OK] Supplement import finished.
) else (
    echo  [WARN] %SUPPLEMENT_SQL_FILE% not found, skipping supplement table.
)

if exist "%RECENT_DATA_SQL_FILE%" (
    echo  Importing recent enoki mushroom batches and cleaning unrelated samples ...
    "%MYSQL_EXE%" -h %DB_HOST% -P %DB_PORT% -u %DB_USER% --default-character-set=utf8mb4 %DB_NAME% < "%RECENT_DATA_SQL_FILE%"
    if errorlevel 1 goto importfail
    echo  [OK] Recent enoki mushroom data imported.
)

"%MYSQL_EXE%" -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='%DB_NAME%';" > "%~dp0db_count.tmp" 2>nul
set /p FINAL_COUNT=<"%~dp0db_count.tmp"
del "%~dp0db_count.tmp" >nul 2>nul
echo  [OK] Database "%DB_NAME%" now has %FINAL_COUNT% table(s).
echo.
echo  ================================================================
echo   Done! Database is ready.
echo  ================================================================
echo.
pause >nul
exit /b 0

:exist
echo  [INFO] Database "%DB_NAME%" already exists with %TABLE_COUNT% table(s).
echo  [INFO] Skipping main import. Nothing to do.

if exist "%SUPPLEMENT_SQL_FILE%" (
    "%MYSQL_EXE%" -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='%DB_NAME%' AND table_name='trace_supplement';" > "%~dp0db_supp.tmp" 2>nul
    for /f "usebackq delims=" %%L in ("%~dp0db_supp.tmp") do set "SUPP_EXISTS=%%L"
    del "%~dp0db_supp.tmp" >nul 2>nul
    if not defined SUPP_EXISTS set "SUPP_EXISTS=0"
    if "!SUPP_EXISTS!"=="0" (
        echo  [INFO] Supplement table missing, importing %SUPPLEMENT_SQL_FILE% ...
        "%MYSQL_EXE%" -h %DB_HOST% -P %DB_PORT% -u %DB_USER% --default-character-set=utf8mb4 %DB_NAME% < "%SUPPLEMENT_SQL_FILE%"
        if errorlevel 1 goto importfail
        echo  [OK] Supplement import finished.
    ) else (
        echo  [INFO] Supplement table already exists, skipping.
    )
) else (
    echo  [WARN] %SUPPLEMENT_SQL_FILE% not found, skipping supplement table.
)
if exist "%RECENT_DATA_SQL_FILE%" (
    echo  [INFO] Refreshing recent enoki mushroom batches ...
    "%MYSQL_EXE%" -h %DB_HOST% -P %DB_PORT% -u %DB_USER% --default-character-set=utf8mb4 %DB_NAME% < "%RECENT_DATA_SQL_FILE%"
    if errorlevel 1 goto importfail
    echo  [OK] Recent enoki mushroom data refreshed.
)
echo.
pause >nul
exit /b 0

:connfail
echo  [ERROR] Cannot connect to MySQL. Check password/credentials.
type "%~dp0db_conn.err" 2>nul
echo.
echo   If the password is not "root", re-run with:
echo     set DB_PWD=your_password
echo     init-db.bat
del "%~dp0db_conn.err" >nul 2>nul
pause
exit /b 1

:createfail
echo  [ERROR] Failed to create database.
del "%~dp0db_conn.err" >nul 2>nul
pause
exit /b 1

:importfail
echo  [ERROR] Import failed.
del "%~dp0db_conn.err" >nul 2>nul
pause
exit /b 1
