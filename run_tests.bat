@echo off
echo Running Multi-Agent Movie Booking System Tests...
echo.

echo Compiling source files...
javac -cp "libs/jade.jar;src" src/*.java

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo Running system tests...
java -cp "libs/jade.jar;src" SystemTest

echo.
echo Tests completed.
pause
