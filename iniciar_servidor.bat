@echo off
REM GR06: levanta MySQL (Docker, puerto 3307) y Payara 5 (8080) con las apps ya desplegadas.
REM Para que arranque solo al iniciar sesion: Win+R, shell:startup y copia ahi un acceso directo a este archivo.
start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"
:esperar_docker
docker info >nul 2>&1 || (timeout /t 5 /nobreak >nul & goto esperar_docker)
docker start mysql_banquito_gr06
call "C:\payara5\bin\asadmin.bat" start-domain domain1
