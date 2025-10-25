@echo off
echo Starting Mini Tank Fire Game...

REM Start backend server in background
cd backend
start /b mvn exec:java
cd ..

REM Wait a bit for backend to start
timeout /t 3 /nobreak > nul

REM Start frontend server in background
cd frontend
start /b python -m http.server 3000
cd ..

REM Wait a bit for frontend to start
timeout /t 2 /nobreak > nul

REM Open browser
start http://localhost:3000

echo Game is running! Backend on ws://localhost:8080, Frontend on http://localhost:3000
echo Press any key to exit this window (servers will keep running)
pause > nul