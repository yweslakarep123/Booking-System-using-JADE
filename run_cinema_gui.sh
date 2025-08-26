#!/bin/bash

echo "Starting Enhanced Cinema GUI..."
echo

echo "Compiling source files..."
javac -cp "libs/jade.jar:src" src/*.java

if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

echo
echo "Starting Enhanced Cinema GUI..."
java -cp "libs/jade.jar:src" EnhancedCinemaGUI

echo
echo "Enhanced Cinema GUI stopped."
