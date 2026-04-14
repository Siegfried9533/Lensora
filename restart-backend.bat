@echo off
setlocal

cd /d "%~dp0"

echo [Lensora] Restarting backend and database...

echo [1/2] Stopping services...
docker compose stop backend db
if errorlevel 1 goto :error

echo [2/2] Starting services...
docker compose up -d --build db backend
if errorlevel 1 goto :error

echo.
echo [Lensora] Current service status:
docker compose ps
if errorlevel 1 goto :error

echo.
echo [Lensora] Backend should be available at: http://localhost:8080
exit /b 0

:error
echo.
echo [Lensora] Failed to restart backend stack.
exit /b 1
