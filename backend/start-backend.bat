@echo off
setlocal

cd /d "%~dp0"
call run-backend.bat %1
exit /b %errorlevel%
