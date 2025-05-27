@echo off
cd src
echo Compiling Java files...
javac *.java

if %errorlevel% neq 0 (
    echo Compilation failed. Press any key to exit.
    pause
    exit /b
)

echo Launching the game...
java Main
pause