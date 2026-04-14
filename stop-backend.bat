@echo off
setlocal

cd /d "%~dp0"

echo [Lensora] Stopping backend and database...
docker compose stop backend db
if errorlevel 1 goto :error

echo.
echo [Lensora] Current service status:
docker compose ps
if errorlevel 1 goto :error

exit /b 0

:error
echo.
echo [Lensora] Failed to stop backend stack.
exit /b 1
