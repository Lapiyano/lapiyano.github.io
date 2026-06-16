@echo off
set JFX_PATH=C:\javafx-sdk-21.0.11\lib
set MODULES=javafx.controls,javafx.graphics,javafx.fxml
set SQLITE_JAR=src/lib/sqlite-jdbc-3.53.0.0.jar

:: This script is intended to be run from the parent directory: C:\Users\morri\minispace\AMini
:: If you are running it from within 'src', it will attempt to go up one level.
if exist "..\src" (
    cd ..
)

:: Create output directory in the root
if not exist bin mkdir bin

echo --- Compiling Source Files ---
:: List all java files in src
dir /s /b src\*.java > sources.txt

:: Compile with src as sourcepath and bin as output
javac --module-path "%JFX_PATH%" --add-modules %MODULES%,java.sql,java.desktop -cp "%SQLITE_JAR%" -sourcepath src -d bin @sources.txt

if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed.
    del sources.txt
    pause
    exit /b %errorlevel%
)
del sources.txt

echo --- Running app.UI ---
:: Run from root with bin and the sqlite jar in classpath
java --module-path "%JFX_PATH%" --add-modules %MODULES%,java.sql,java.desktop -cp "bin;%SQLITE_JAR%" app.UI

pause
