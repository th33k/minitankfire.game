@echo off
echo Building Tank Game Server...

REM Build server
cd server
mvn clean compile
cd ..

echo Build complete!