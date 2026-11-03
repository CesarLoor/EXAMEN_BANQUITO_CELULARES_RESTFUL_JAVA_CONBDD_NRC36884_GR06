@echo off
REM Compila los proyectos Maven y despliega los 3 WAR en Payara 5 (GR06).
REM Requisito: MySQL en Docker levantado (cd "02. BDD" && docker compose up -d)
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

call "%ASADMIN%" deploy --force=true --contextroot BANQUITO_CORE "%RAIZ%04. SERVIDOR\BANQUITO_CORE_RESTFUL_JAVA_GR06\target\BANQUITO_CORE.war" || exit /b 1
call "%ASADMIN%" deploy --force=true --contextroot COMERCIALIZADORA "%RAIZ%04. SERVIDOR\COMERCIALIZADORA_RESTFUL_JAVA_GR06\target\COMERCIALIZADORA.war" || exit /b 1
call "%ASADMIN%" deploy --force=true --contextroot COMERCIALIZADORA_WEB "%RAIZ%04. WEB\COMERCIALIZADORA_RESTFUL_JAVA_WEB\target\COMERCIALIZADORA_WEB.war" || exit /b 1

echo.
echo Listo:
echo   BanQuito (pruebas):   http://localhost:8080/BANQUITO_CORE/
echo   API Comercializadora: http://localhost:8080/COMERCIALIZADORA/api/telefonos
echo   Cliente web:          http://localhost:8080/COMERCIALIZADORA_WEB
endlocal
