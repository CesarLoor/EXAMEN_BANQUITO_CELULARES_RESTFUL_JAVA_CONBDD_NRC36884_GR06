@echo off
REM Crea el contenedor MySQL 8.4 (puerto 3307), carga las BDD banquito + comercializadora y verifica los datos.
REM Uso:  levantar_bdd.bat          -> crea/enciende (si ya existe, conserva los datos)
REM       levantar_bdd.bat reset    -> borra todo y vuelve a cargar los datos iniciales
setlocal
cd /d "%~dp0"
set C=mysql_banquito_gr06

docker info >nul 2>&1
if errorlevel 1 (
    echo [X] Docker Desktop no esta abierto. Abrelo, espera "Engine running" y vuelve a ejecutar este archivo.
    goto fin
)

if /i "%~1"=="reset" (
    echo === Borrando contenedor y datos anteriores...
    docker compose down -v
)

echo === Creando/encendiendo el contenedor %C% ...
docker compose up -d
if errorlevel 1 (
    echo [X] No se pudo crear el contenedor. Si dice "port is already allocated", el puerto 3307 esta ocupado.
    goto fin
)

echo === Esperando a que MySQL cargue los scripts (la primera vez tarda 20-60 s)...
REM Por TCP solo responde cuando ya termino de ejecutar los .sql de scripts/
for /l %%i in (1,1,90) do (
    docker exec %C% mysql -h127.0.0.1 -ugr06 -pgr06pass -N -e "SELECT 1" >nul 2>&1 && goto listo
    ping -n 3 127.0.0.1 >nul
)
echo [X] MySQL no respondio en 3 minutos. Revisa el log con:  docker logs %C%
goto fin

:listo
echo.
docker exec %C% mysql -h127.0.0.1 -ugr06 -pgr06pass -t -e "SELECT (SELECT COUNT(*) FROM banquito.CLIENTE) clientes_banco, (SELECT COUNT(*) FROM banquito.MOVIMIENTO) movimientos, (SELECT COUNT(*) FROM banquito.CREDITO) creditos, (SELECT COUNT(*) FROM comercializadora.TELEFONO) telefonos, (SELECT COUNT(*) FROM comercializadora.FACTURA) facturas" 2>nul
echo.
echo [OK] Base de datos lista en localhost:3307   usuario gr06 / gr06pass   (root / root)
echo      Datos iniciales: 7 clientes, 60 movimientos, 1 credito, 10 telefonos, 0 facturas.

:fin
echo.
pause
endlocal
