#!/bin/bash

echo "Starting Multi-Agent Movie Booking System..."
echo

echo "Compiling source files..."
javac -cp "libs/jade.jar:src" src/*.java

if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

echo
echo "Starting Enhanced Main Container..."
java -cp "libs/jade.jar:src" EnhancedMainContainer

echo
echo "System stopped."
