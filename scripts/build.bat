@echo off
echo Building Mini Tank Fire Game...

REM Build server
cd server
mvn clean compile
cd ..

echo Build complete!