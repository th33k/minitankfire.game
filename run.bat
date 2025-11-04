@echo off
echo Starting Mini Tank Fire Game...

REM Start server in background
cd server
start /b mvn exec:java
cd ..

REM Wait a bit for server to start
timeout /t 3 /nobreak > nul

REM Start client server in background
cd client
start /b python -m http.server 3000 --bind 0.0.0.0
cd ..

REM Wait a bit for client to start
timeout /t 2 /nobreak > nul

REM Open browser
start http://localhost:3000

echo Game is running! Server on ws://localhost:8080, Client on http://localhost:3000
echo For LAN multiplayer: Tell friends to visit http://[YOUR_LAN_IP]:3000 and enter your LAN IP in the server address field
echo Press any key to exit this window (servers will keep running)
pause > nul