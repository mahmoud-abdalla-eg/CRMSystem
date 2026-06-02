@echo off
cd /d "%~dp0"
echo Compiling CRM System...
javac -d bin -sourcepath src src\main\Main.java src\views\MainFrame.java src\models\*.java src\utils\*.java
if errorlevel 1 (
    echo Compilation failed!
    pause
    exit /b 1
)
echo Running CRM System...
java -cp bin main.Main
pause
