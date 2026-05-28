@echo off
setlocal

cd /d "%~dp0"

echo [Lensora] Restarting Docker stack...

echo [1/2] Stopping services...
call stop-backend.bat
if errorlevel 1 goto :error_stop

echo [2/2] Rebuilding and starting services...
call run-backend.bat %1
if errorlevel 1 goto :error_run

exit /b 0

:error_stop
echo.
echo [Lensora] Failed while stopping Docker stack.
exit /b 1

:error_run
echo.
echo [Lensora] Failed while starting Docker stack.
exit /b 1
