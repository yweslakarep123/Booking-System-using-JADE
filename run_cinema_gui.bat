@echo off
echo Starting Enhanced Cinema GUI...
echo.

echo Compiling source files...
javac -cp "libs/jade.jar;src" src/*.java

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo Starting Enhanced Cinema GUI...
java -cp "libs/jade.jar;src" EnhancedCinemaGUI

echo.
echo Enhanced Cinema GUI stopped.
pause
