@echo off
setlocal

cd /d "%~dp0"

set "DB_PORT=5433"
if not "%~1"=="" set "DB_PORT=%~1"

set "PORT_PID="
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /R /C:":%DB_PORT% .*LISTENING"') do (
	set "PORT_PID=%%P"
	goto :port_in_use
)
goto :port_available

:port_in_use
echo [Lensora] Port %DB_PORT% is already in use by PID %PORT_PID%.
echo [Lensora] Stop that process or run this script with another port, for example:
echo run-backend.bat 5434
exit /b 1

:port_available
echo [Lensora] Starting Docker stack...
echo [Lensora] PostgreSQL will be published on host port: %DB_PORT%
docker compose up -d --build
if errorlevel 1 goto :error

echo.
echo [Lensora] Current service status:
docker compose ps
if errorlevel 1 goto :error

echo.
echo [Lensora] Backend should be available at: http://localhost:8080
echo [Lensora] PostgreSQL should be available at: localhost:%DB_PORT%
echo [Lensora] Use this command to view logs:
echo docker compose logs -f backend

exit /b 0

:error
echo.
echo [Lensora] Failed to start Docker stack.
exit /b 1
