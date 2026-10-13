@echo off
setlocal EnableExtensions EnableDelayedExpansion
chcp 65001 >nul 2>nul

set "ROOT=%~dp0.."
set "SQL_FILE=%ROOT%\db\enokimushroom.sql"

set "DB_HOST=%~1"
set "DB_PORT=%~2"
set "DB_USER=%~3"
set "DB_PASSWORD=%~4"
set "DB_NAME=%~5"

if "%DB_HOST%"=="" set "DB_HOST=127.0.0.1"
if "%DB_PORT%"=="" set "DB_PORT=3306"
if "%DB_USER%"=="" set "DB_USER=root"
if "%DB_PASSWORD%"=="" set "DB_PASSWORD=root"
if "%DB_NAME%"=="" set "DB_NAME=enokimushroom"

if not exist "%SQL_FILE%" (
  echo [ERROR] Database dump not found: %SQL_FILE%
  exit /b 1
)

set "MYSQL_CLIENT="
if exist "%ROOT%\env\mysql\bin\mysql.exe" set "MYSQL_CLIENT=%ROOT%\env\mysql\bin\mysql.exe"
if not defined MYSQL_CLIENT (
  where mysql >nul 2>nul
  if errorlevel 1 (
    echo [ERROR] mysql.exe not found in PATH.
    echo         Add the MySQL bin directory to PATH and try again.
    exit /b 1
  )
  set "MYSQL_CLIENT=mysql"
)

set "MYSQL_PWD=%DB_PASSWORD%"
"%MYSQL_CLIENT%" -h "%DB_HOST%" -P "%DB_PORT%" -u "%DB_USER%" --protocol=TCP -e "SELECT VERSION();" >nul 2>nul
if errorlevel 1 (
  set "MYSQL_PWD="
  echo [ERROR] Cannot connect to MySQL.
  echo         Check DB_HOST, DB_PORT, DB_USER and DB_PASSWORD.
  exit /b 1
)

echo.
echo WARNING: This replaces all tables in database "%DB_NAME%".
echo To confirm, type the exact database name: %DB_NAME%
set "CONFIRM="
set /p "CONFIRM=> "
if /I not "%CONFIRM%"=="%DB_NAME%" (
  set "MYSQL_PWD="
  echo Cancelled. No database changes were made.
  exit /b 0
)

echo.
echo Creating database "%DB_NAME%"...
"%MYSQL_CLIENT%" -h "%DB_HOST%" -P "%DB_PORT%" -u "%DB_USER%" --protocol=TCP --default-character-set=utf8mb4 -e "CREATE DATABASE IF NOT EXISTS `%DB_NAME%` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
if errorlevel 1 goto database_error

echo Importing database. This may take several minutes...
"%MYSQL_CLIENT%" -h "%DB_HOST%" -P "%DB_PORT%" -u "%DB_USER%" --protocol=TCP --default-character-set=utf8mb4 "%DB_NAME%" < "%SQL_FILE%"
if errorlevel 1 goto database_error

set "MYSQL_PWD="
echo.
echo [OK] Database "%DB_NAME%" initialized successfully.
exit /b 0

:database_error
set "MYSQL_PWD="
echo.
echo [ERROR] Database import failed. Check the MySQL account permissions and output above.
exit /b 1
