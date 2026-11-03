@echo off
chcp 65001 >nul
java -jar "%~dp0target\COMERCIALIZADORA_CONSOLA.jar" %*
pause
