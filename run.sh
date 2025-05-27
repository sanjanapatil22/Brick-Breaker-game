#!/bin/bash
cd src
echo "Compiling..."
javac *.java

if [ $? -ne 0 ]; then
  echo "Compilation failed. Exiting."
  exit 1
fi

echo "Running game..."
java Main