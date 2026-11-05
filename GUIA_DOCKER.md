# Guía: base de datos en Docker (BanQuito + Comercializadora, GR06)

Con esta guía levantas en tu PC las **2 bases de datos MySQL** del proyecto (`banquito` y `comercializadora`), ya creadas y con datos de prueba. No hace falta instalar MySQL ni ejecutar scripts a mano.

## 1. Requisitos

| Programa | Para qué | Dónde |
|---|---|---|
| **Docker Desktop** (con WSL 2) | Corre MySQL 8.4 en un contenedor | https://www.docker.com/products/docker-desktop/ |
| **Git** | Bajar el proyecto | https://git-scm.com/ |
| JDK 17 + Maven + Payara 5.2022.5 | Solo si también vas a desplegar los servidores (paso 6) | `C:\payara5` |

Después de instalar Docker Desktop, **ábrelo y espera** a que diga *Engine running*. Compruébalo en una terminal:

```bash
docker --version
docker compose version
```

## 2. Bajar el proyecto

```bash
git clone https://github.com/CesarLoor/EXAMEN_BANQUITO_CELULARES_RESTFUL_JAVA_CONBDD_NRC36884_GR06.git
```

Sin Git: en esa página, **Code → Download ZIP** y descomprímelo.

## 3. Levantar la base de datos

### Opción rápida (recomendada): doble clic

Abre la carpeta `02. BDD` y haz **doble clic en `levantar_bdd.bat`**. El script:

1. Revisa que Docker Desktop esté abierto.
2. Crea el contenedor `mysql_banquito_gr06` (MySQL 8.4, puerto 3307).
3. Crea las 2 bases, las tablas, el usuario `gr06` y carga los datos de prueba.
4. Espera a que terminen los scripts y muestra el conteo de datos.

Al final debe verse:

```
| clientes_banco | movimientos | creditos | telefonos | facturas |
|              7 |          60 |        1 |        10 |        0 |
[OK] Base de datos lista en localhost:3307   usuario gr06 / gr06pass   (root / root)
```

Si se vuelve a ejecutar, solo enciende el contenedor y **conserva los datos**. Para volver a los datos iniciales, desde una terminal en `02. BDD`:

```bash
levantar_bdd.bat reset
```

Con eso ya puedes pasar al paso 5. Los pasos manuales de abajo hacen lo mismo, por si prefieres los comandos.

### Opción manual

Entra a la carpeta `02. BDD` (lleva comillas porque tiene espacio) y arranca el contenedor:

```bash
cd "EXAMEN_BANQUITO_CELULARES_RESTFUL_JAVA_CONBDD_NRC36884_GR06/02. BDD"
```

```bash
docker compose up -d
```

La primera vez descarga la imagen `mysql:8.4` (ocupa ~1 GB en disco) y ejecuta los scripts de `02. BDD/scripts/` **en orden**:

1. `01_banquito_core.sql`: BDD `banquito` (CLIENTE, CUENTA, MOVIMIENTO, CREDITO, AMORTIZACION) y el usuario `gr06`.
2. `02_comercializadora.sql`: BDD `comercializadora` (CLIENTE, TELEFONO, FORMA_PAGO, FACTURA, DETALLE_FACTURA).

Espera unos 20–30 segundos y revisa que esté **Up**:

```bash
docker ps
```

Debe aparecer `mysql_banquito_gr06` con `0.0.0.0:3307->3306/tcp`.

## 4. Comprobar que se cargaron los datos

```bash
docker exec mysql_banquito_gr06 mysql -ugr06 -pgr06pass -e "SELECT COUNT(*) clientes FROM banquito.CLIENTE; SELECT COUNT(*) telefonos FROM comercializadora.TELEFONO;"
```

Debe mostrar **7 clientes** y **10 teléfonos**. El aviso `Using a password on the command line interface can be insecure` es normal. Si sale `Access denied` o `Unknown database`, espera unos segundos más: MySQL todavía está ejecutando los scripts.

## 5. Datos de conexión

| Dato | Valor |
|---|---|
| Host | `localhost` |
| Puerto | **3307** (no 3306, para no chocar con un MySQL/MariaDB local) |
| Usuario de la aplicación | `gr06` / `gr06pass` |
| Usuario administrador | `root` / `root` |
| Bases | `banquito`, `comercializadora` |
| JDBC BanQuito | `jdbc:mysql://localhost:3307/banquito` |
| JDBC Comercializadora | `jdbc:mysql://localhost:3307/comercializadora` |

Para verlas en **MySQL Workbench**, DBeaver o el panel *Services* de NetBeans, crea una conexión con esos datos. En DBeaver activa `allowPublicKeyRetrieval=true` en las propiedades del driver.

## 6. Desplegar los servidores (opcional)

Con la BDD arriba, desde la raíz del proyecto:

```bash
desplegar.bat
```

El script inicia Payara, compila con Maven y despliega los 3 WAR. Los servidores ya apuntan a `localhost:3307`, así que no hay que configurar nada.

Si la BDD está en **otra máquina**, define estas variables de entorno antes de iniciar Payara (cambia `IP`):

| Variable | Ejemplo |
|---|---|
| `BANQUITO_DB_URL` | `jdbc:mysql://IP:3307/banquito?useSSL=false&allowPublicKeyRetrieval=true` |
| `COMERCIALIZADORA_DB_URL` | `jdbc:mysql://IP:3307/comercializadora?useSSL=false&allowPublicKeyRetrieval=true` |

## 7. Comandos del día a día

Se ejecutan dentro de `02. BDD`:

| Qué quieres hacer | Comando |
|---|---|
| Encender | `docker compose up -d` |
| Apagar (los datos se conservan) | `docker compose stop` |
| Ver el log de MySQL | `docker logs -f mysql_banquito_gr06` |
| Entrar a la consola de MySQL | `docker exec -it mysql_banquito_gr06 mysql -uroot -proot` |
| **Volver a los datos iniciales** (antes de la defensa) | `levantar_bdd.bat reset`, o `docker compose down -v` y luego `docker compose up -d` |

`down -v` **borra el volumen**: se pierden las facturas y créditos de prueba, y los scripts se vuelven a ejecutar desde cero.

El contenedor tiene `restart: unless-stopped`: arranca solo cuando abres Docker Desktop.

## 8. Problemas comunes

| Síntoma | Causa y solución |
|---|---|
| `docker: command not found` o `error during connect` | Docker Desktop no está abierto. Ábrelo y espera *Engine running*. |
| `port is already allocated` (3307) | Otro programa usa el 3307. Cambia en `docker-compose.yml` `"3307:3306"` por `"3308:3306"` y usa ese puerto en las URLs JDBC (paso 6). |
| `container name "/mysql_banquito_gr06" is already in use` | Quedó un contenedor viejo: `docker rm -f mysql_banquito_gr06` y otra vez `docker compose up -d`. |
| Cambié los `.sql` pero no se reflejan | Los scripts solo corren cuando el volumen está vacío. Usa `docker compose down -v` y `docker compose up -d`. |
| Tildes raras (`Ã¡`) | Se cargó con un volumen antiguo. Haz el reset del paso 7. |
| Los servidores dan `Communications link failure` | El contenedor está apagado o el puerto no coincide. Revisa `docker ps`. |
| Las fechas de "último mes" no cuadran | Los datos usan fechas relativas a `CURDATE()`. Haz el reset del paso 7 el mismo día de la defensa. |

## Cédulas de prueba

| Cédula | Resultado esperado |
|---|---|
| 1712345678 | Sujeto de crédito, máx. $1,155.00 |
| 1767890123 | Sujeto de crédito, máx. $2,880.00 |
| 1734567890 | Rechazado: hombre menor de 25 |
| 1745678901 | Rechazado: crédito activo (tiene la tabla #1 para consultar) |

Login de los clientes: **MONSTER / MONSTER69**.
