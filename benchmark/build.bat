@echo off
REM Script para compilar y ejecutar el proyecto Java desde Windows CMD.
REM Usar un JDK moderno (Java 17+), tu JDK 25 está bien.

cd /d "%~dp0"

echo Verificando Java... 
java -version
javac -version
echo.

if not exist out mkdir out
if exist sources.txt del /f /q sources.txt

echo Listando archivos fuente...
dir /s /b src\*.java > sources.txt

echo Compilando...
javac --release 17 -d out @sources.txt
if errorlevel 1 (
    echo.
    echo ERROR: la compilacion fallo.
    pause
    exit /b 1
)

echo.
echo Ejecutando la aplicacion...
java -cp out Main
pause
