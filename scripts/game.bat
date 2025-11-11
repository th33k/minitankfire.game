@echo off
setlocal

set SCRIPT_DIR=%~dp0
set PROJECT_ROOT=%SCRIPT_DIR%..

cd /d %PROJECT_ROOT%

if "%1"=="" (
    set ACTION=build
) else (
    set ACTION=%1
)

if "%ACTION%"=="build" goto build
if "%ACTION%"=="run" goto run
if "%ACTION%"=="stop" goto stop

echo Usage: game {build|run|stop}
goto end

:build
echo Building Tank Game Server...

REM Build server
cd server
mvn clean compile
cd ..

echo Build complete!
goto end

:run
echo Starting Tank Game Server...

REM Start server in background
cd server
start /b mvn exec:java
cd ..

REM Wait a bit for server to start
timeout /t 5 /nobreak > nul

REM Start client web server in background
cd client
start /b python -m http.server 3000 --bind 0.0.0.0
cd ..

REM Wait a bit for client to start
timeout /t 2 /nobreak > nul

REM Open browser
start http://localhost:3000

echo ============================================
echo Tank Game is running!
echo ============================================
echo Server running on:
echo   TCP: localhost:8080 (reliable connections)
echo   UDP: localhost:8081 (real-time updates)
echo   NIO: localhost:8082 (scalable connections)
echo Client web server: http://localhost:3000
goto end

:stop
echo Stopping Tank Game Servers...

REM Kill Java processes
taskkill /f /im java.exe > nul 2>&1

REM Kill Python processes (web server)
taskkill /f /im python.exe > nul 2>&1

echo Servers stopped.
goto end

:end
endlocal