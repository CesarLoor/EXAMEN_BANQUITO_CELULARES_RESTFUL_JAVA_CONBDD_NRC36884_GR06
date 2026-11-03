@echo off
REM Compila los proyectos Maven y despliega los 3 WAR en Payara 5 (GR06).
REM Requisito: MySQL en Docker levantado (cd "02. BDD" && docker compose up -d)
REM Payara escucha en 0.0.0.0:8080, asi que otras maquinas de la misma red entran con la IP de esta PC.
setlocal
set ASADMIN=C:\payara5\bin\asadmin.bat
set RAIZ=%~dp0

call "%ASADMIN%" start-domain domain1

for %%P in ("04. SERVIDOR\BANQUITO_CORE_RESTFUL_JAVA_GR06" "04. SERVIDOR\COMERCIALIZADORA_RESTFUL_JAVA_GR06" "04. WEB\COMERCIALIZADORA_RESTFUL_JAVA_WEB" "04. CLICON\COMERCIALIZADORA_RESTFUL_JAVA_CONSOLA" "04. CLIESC\COMERCIALIZADORA_RESTFUL_JAVA_ESCRITORIO") do (
    echo === Compilando %%~P
    pushd "%RAIZ%%%~P" || exit /b 1
    call mvn -q -B package || (popd & exit /b 1)
    popd
)

REM El context-root sale de WEB-INF/glassfish-web.xml (el mismo que usa NetBeans al hacer Run)
for %%W in ("04. SERVIDOR\BANQUITO_CORE_RESTFUL_JAVA_GR06\target\BANQUITO_CORE_RESTFUL_JAVA_GR06.war" "04. SERVIDOR\COMERCIALIZADORA_RESTFUL_JAVA_GR06\target\COMERCIALIZADORA_RESTFUL_JAVA_GR06.war" "04. WEB\COMERCIALIZADORA_RESTFUL_JAVA_WEB\target\COMERCIALIZADORA_RESTFUL_JAVA_WEB.war") do (
    call "%ASADMIN%" deploy --force=true "%RAIZ%%%~W" || exit /b 1
)

echo.
echo Listo. Desde esta PC:
echo   BanQuito (pruebas):   http://localhost:8080/BANQUITO_CORE_RESTFUL_JAVA_GR06/
echo   API Comercializadora: http://localhost:8080/COMERCIALIZADORA_RESTFUL_JAVA_GR06/webresources/telefonos
echo   Cliente web:          http://localhost:8080/COMERCIALIZADORA_RESTFUL_JAVA_WEB/
echo.
echo Desde otras maquinas o celulares de la misma red, cambia localhost por la IP de esta PC:
powershell -NoProfile -Command "Get-NetIPConfiguration | ? IPv4DefaultGateway | %% { $ip = $_.IPv4Address.IPAddress; '  [' + $_.InterfaceAlias + ']  web: http://' + $ip + ':8080/COMERCIALIZADORA_RESTFUL_JAVA_WEB/   clientes: servidor ' + $ip + ':8080' }"
endlocal
