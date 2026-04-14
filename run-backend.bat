@echo off
setlocal

cd /d "%~dp0"

echo [Lensora] Starting database and backend...
docker compose up -d --build db backend
if errorlevel 1 goto :error

echo.
echo [Lensora] Current backend status:
docker compose ps backend
if errorlevel 1 goto :error

echo.
echo [Lensora] Backend should be available at: http://localhost:8080
echo [Lensora] Use this command to view logs:
echo docker compose logs -f backend

exit /b 0

:error
echo.
echo [Lensora] Failed to start backend stack.
exit /b 1
