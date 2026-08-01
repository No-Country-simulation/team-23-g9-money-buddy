# Money Buddy

Money Buddy es una aplicación para analizar información financiera personal a partir de ingresos, deudas y transacciones. El proyecto incluye una API REST con Java 21 y Spring Boot 3, y un dashboard demo construido con Vite, React y TypeScript.

## Quick start con Docker Compose

Docker Compose es el flujo recomendado para levantar la aplicación completa sin configurar Maven o Node localmente.

### Requisitos

- Git
- Docker Desktop instalado y abierto

### Ejecutar aplicación completa

```bash
git clone <url-del-repositorio>
cd team-23-g9-money-buddy
docker compose up --build
```

Si ya tienes el repositorio local:

```bash
git pull
docker compose up --build
```

Servicios disponibles:

| Servicio | URL |
| --- | --- |
| Frontend | `http://localhost:5173` |
| API | `http://localhost:8080` |
| Health check | `http://localhost:8080/actuator/health` |

El frontend Docker se compila con `VITE_API_BASE_URL=http://localhost:8080` para que el navegador llame al backend desde tu máquina, no usando el hostname interno de Docker.

Para detener los servicios:

```bash
docker compose down
```

## Desarrollo local

Usa estos comandos cuando necesites ejecutar frontend o backend fuera de Docker.

### Frontend

Requisitos locales:

- Node.js 22+
- npm 11+

```bash
cd frontend
npm install
npm run dev
```

El frontend queda disponible en la URL que indique Vite, normalmente `http://localhost:5173`.

Por defecto envía el análisis a `http://localhost:8080/analisis-financiero`. Para cambiar la URL base del backend, crear `frontend/.env` tomando como referencia `frontend/.env.example`:

```env
VITE_API_BASE_URL=http://localhost:8080
```

### Backend

Requisitos locales:

- JDK 21
- Maven 3.9+

```bash
cd backend
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

## API principal

`POST /analisis-financiero` recibe datos financieros en JSON y devuelve un análisis inicial con perfil financiero, score, resumen de gastos, indicadores, transacciones clasificadas y recomendaciones.

El endpoint es stateless: calcula el resultado usando únicamente el request actual, sin persistencia ni base de datos.

Contrato detallado: [`docs/analisis-financiero-api.md`](docs/analisis-financiero-api.md).

### Endpoints iniciales

| Endpoint | Uso |
| --- | --- |
| `GET /api` | Estado base de la API. |
| `POST /analisis-financiero` | Análisis financiero inicial. |
| `GET /actuator/health` | Health check de Spring Actuator. |

Para verificar los endpoints base con la aplicación levantada:

```bash
curl http://localhost:8080/api
curl http://localhost:8080/actuator/health
```

El health check debe responder con estado `UP`.

## Validaciones

`POST /analisis-financiero` rechaza requests incompletos o inválidos con estado `400`.

Resumen de reglas principales:

- Los campos raíz obligatorios son `credito_total`, `ingreso_mensual`, `frecuencia_ahorro`, `pago_mensual_deudas` y `transacciones`.
- `transacciones` debe incluir al menos una transacción.
- Cada transacción debe incluir `tipo`, `fecha`, `descripcion` y `monto`.
- `tipo_pago` es obligatorio cuando `tipo` es `Egreso`.
- `meses_a_deber` es obligatorio cuando `tipo_pago` es `Credito`.

Validaciones completas y formato de error: [`docs/analisis-financiero-api.md#validaciones-esperadas`](docs/analisis-financiero-api.md#validaciones-esperadas).

## Pruebas

### Backend

```bash
cd backend
mvn test
```

Las pruebas automatizadas en `AnalisisFinancieroControllerTest` deben cubrir el contrato oficial de `POST /analisis-financiero`, casos válidos, validaciones y errores con HTTP `400`.

### Frontend

El frontend no tiene un script de pruebas configurado actualmente. Para verificar compilación:

```bash
cd frontend
npm run build
```

## Documentación adicional

- [Contrato detallado de `POST /analisis-financiero`](docs/analisis-financiero-api.md)
- [Mapeo Data Science Backend para `/analisis-financiero`](docs/analisis-financiero-ds-backend-mapping.md)
- [OCI Object Storage Free Tier para artefactos](docs/oci-object-storage-artefactos.md)
- [Guía de contribución](CONTRIBUTING.md)
