# BanQuito Módulo de Crédito + Comercializadora de Celulares — RESTful Java con BDD

**Grupo 6 · NRC 36884** — Llumigusin Caiza Mateo Sebastián · Mideros Cruz Samir Esteban · Loor Mercado César Ignacio

Dos aplicaciones web independientes que se comunican por servicios REST:

- **BanQuito Core** (módulo de crédito)
- **Comercializadora de Teléfonos Celulares** (facturación)

Cada una tiene su propia base de datos MySQL. Además hay 4 clientes: consola, escritorio, web y móvil.

```
01. UML/            Diagramas (.puml fuente + .png): casos de uso, E-R, modelo del Core, arquitectura, secuencia
02. BDD/            docker-compose.yml + scripts/01_banquito_core.sql + scripts/02_comercializadora.sql
04. SERVIDOR/       BANQUITO_CORE_RESTFUL_JAVA_GR06      -> /BANQUITO_CORE/api      (+ página de pruebas en /BANQUITO_CORE/)
                    COMERCIALIZADORA_RESTFUL_JAVA_GR06   -> /COMERCIALIZADORA/api
04. CLICON/         Cliente consola      (Java 17 + Gson)
04. CLIESC/         Cliente escritorio   (Swing + FlatLaf)
04. WEB/            Cliente web          (JSP + Servlets)  -> /COMERCIALIZADORA_WEB
04. CLIMOV/         Cliente móvil        (Android, Java)
05. DOCUMENTACION/  Documento Word/PDF: arquitectura, casos de uso, ECUD, BDD, E-R, servicios, clientes, matriz de rúbrica
desplegar.bat       Compila todo con Maven y despliega los 3 WAR en Payara 5
```

## Arranque rápido

1. `cd "02. BDD"` y `docker compose up -d` — MySQL 8.4 en el puerto **3307** (el 3306 lo usa MariaDB local).
2. Ejecuta `desplegar.bat`. Inicia Payara 5 (`C:\payara5`), compila y despliega BANQUITO_CORE, COMERCIALIZADORA y COMERCIALIZADORA_WEB.
3. Clientes:
   - **Consola:** `04. CLICON\...\ejecutar.bat`
   - **Escritorio:** `04. CLIESC\...\ejecutar.bat`
   - **Web:** http://localhost:8080/COMERCIALIZADORA_WEB
   - **Móvil:** abre el proyecto en Android Studio y ejecútalo.
     - Emulador: servidor `10.0.2.2:8080`.
     - Celular por USB: corre `adb reverse tcp:8080 tcp:8080` y usa el servidor `127.0.0.1:8080`.
4. Login en todos los clientes: **MONSTER / MONSTER69**.

Para volver a los datos iniciales antes de la defensa:

```bash
docker compose down -v && docker compose up -d
```

## Cédulas de prueba (BanQuito)

| Cédula | Cliente | Resultado |
|---|---|---|
| 1712345678 | Carlos (M, 41) | Sujeto de crédito, máx. $1,155.00 |
| 1723456789 | María (F, 23) | Sujeto (la regla de 25 años es solo para hombres), máx. $1,057.50 |
| 1734567890 | Luis (M, 22) | NO: hombre menor de 25 |
| 1745678901 | Ana (F, 36) | NO: crédito activo (tiene la tabla #1 para consultar) |
| 1756789012 | Jorge (M, 48) | NO: sin depósitos en el último mes |
| 1767890123 | Sofía (F, 31) | Sujeto, máx. $2,880.00 |
| 1778901234 | Pedro (M, 30) | Sujeto, máx. $180.00 (prueba de "precio > máximo") |

## Decisiones

- **Monto máximo:** `((PromDep − PromRet) × 30%) × 6`. El enunciado dice 35% en el texto pero 30% en la fórmula; se usa la fórmula.
- **Tabla de amortización:**
  - Cuota fija (función PAGO de Excel), 16.5% anual, plazo de 3 a 18 meses.
  - Cada fila se redondea a centavos y la última cuota absorbe el redondeo, así el saldo cierra en 0.00.
  - Se verifica con la tabla del enunciado en `CalculadoraAmortizacionTest`.
- **Descuento por pago en efectivo:** 42%, parametrizado en la tabla `FORMA_PAGO`.

## Mejoras frente al proyecto de referencia (GR03, SOAP)

| GR03 | Este proyecto |
|---|---|
| "Último mes" fijo en `'2024-11-01'` | Fechas relativas a `CURDATE()`, en los datos y en las consultas |
| Rechazaba a **todas las mujeres** (solo aceptaba `M` ≥ 25) | La regla de edad aplica solo a hombres, como pide el enunciado |
| La elegibilidad no revisaba si había un crédito activo | Revisa las 4 reglas y devuelve **todos** los motivos de rechazo |
| `double` sin redondeo; el saldo final no daba 0 | `BigDecimal`, redondeo por fila, saldo final 0.00, con prueba unitaria |
| Teléfonos y ventas dentro de la BDD del banco | BDD independiente de la comercializadora, que solo habla con el banco por REST |
| Venta sin cliente, sin detalle ni descuento guardado | Factura + detalle + forma de pago + número de factura + código de crédito |
| Errores tragados con `printStackTrace` | Respuestas `{"mensaje"}` con códigos HTTP 400/404/422/502/503 |
| Dos créditos simultáneos podían aprobarse | `SELECT … FOR UPDATE` en una transacción: solo se aprueba uno |
| Cliente web en Python | Los 4 clientes en Java, con MVC y el mismo código de modelo/servicio/controlador |
