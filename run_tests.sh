#!/bin/bash

echo "Running Multi-Agent Movie Booking System Tests..."
echo

echo "Compiling source files..."
javac -cp "libs/jade.jar:src" src/*.java

if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

echo
echo "Running system tests..."
java -cp "libs/jade.jar:src" SystemTest

echo
echo "Tests completed."
