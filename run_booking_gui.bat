@echo off
echo Starting Booking GUI...
echo.

echo Compiling source files...
javac -cp "libs/jade.jar;src" src/*.java

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo Starting Booking GUI...
java -cp "libs/jade.jar;src" BookingGUI

echo.
echo Booking GUI stopped.
pause
