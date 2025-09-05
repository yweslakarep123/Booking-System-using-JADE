@echo off
echo Starting Multi-Agent Movie Booking System...
echo.

echo Compiling source files...
javac -cp "libs/jade.jar;src" src/*.java

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo Starting Enhanced Main Container...
java -cp "libs/jade.jar;src" EnhancedMainContainer

echo.
echo System stopped.
pause
