@echo off
echo Stopping Tank Game Servers...

REM Kill Java processes
taskkill /f /im java.exe > nul 2>&1

REM Kill Python processes (web server)
taskkill /f /im python.exe > nul 2>&1

echo Servers stopped.