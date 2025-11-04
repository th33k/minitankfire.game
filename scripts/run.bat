@echo off
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
echo.
echo NOTE: Client needs to be updated to connect to new server endpoints
echo ============================================
echo Press any key to exit this window (servers will keep running)
pause > nul