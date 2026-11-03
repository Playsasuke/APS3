@echo off
setlocal EnableExtensions
title EcoMemory - Rodar

cd /d "%~dp0"

echo.
echo ===== EcoMemory =====
echo Pasta: %CD%
echo.

echo [1/4] Checando Java...
where java
where javac
echo.

echo [2/4] Versoes:
java -version
echo.
javac -version
echo.

if not exist "out" mkdir out

echo [3/4] Compilando...
javac -encoding UTF-8 -d out src\*.java
echo Codigo de retorno do javac: %errorlevel%
echo.

echo [4/4] Executando...
java -cp out Main
echo Codigo de retorno do java: %errorlevel%
echo.

echo ===== FIM =====
pause
endlocal
