# Money Buddy

---

## Backend API

API REST base de Money Buddy construida con Java 21, Spring Boot 3 y Maven.

Esta guía explica cómo levantar el backend localmente después de clonar o actualizar el repositorio.

### Requisitos locales

- Git
- JDK 21
- Maven 3.9+, si se ejecuta el backend sin Docker
- Docker Desktop, si se ejecuta el backend con Docker en una computadora local

Docker Desktop debe estar instalado y abierto antes de usar `docker compose` o `docker build`.

### Qué hace Docker en este proyecto

Docker crea una imagen del backend con Java y la aplicación ya empaquetada. Docker Compose usa esa imagen para iniciar el servicio en el puerto `8080`, sin depender de que cada integrante configure Maven para ejecutar la aplicación manualmente.

### Flujo recomendado

1. Clonar el repositorio o traer los últimos cambios.
2. Entrar a la carpeta del proyecto.
3. Levantar el backend con Docker Compose o con Maven local.
4. Verificar los endpoints base.

```bash
git clone <url-del-repositorio>
cd team-23-g9-money-buddy
```

Si ya tienes el repositorio local:

```bash
git pull
```

### Ejecutar con Docker Compose

```bash
docker compose up --build
```

La API queda disponible en `http://localhost:8080`.

Para detener el servicio:

```bash
docker compose down
```

### Ejecutar con Maven local

```bash
cd backend
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

### Endpoints iniciales

- `GET /api`: estado base de la API.
- `POST /analisis-financiero`: recibe datos financieros en JSON y devuelve un análisis inicial.
- `GET /actuator/health`: health check de Spring Actuator.

### Verificar endpoints

Con la aplicación levantada, ejecutar en otra terminal:

```bash
curl http://localhost:8080/api
curl http://localhost:8080/actuator/health
```

El health check debe responder con estado `UP`.

### Probar análisis financiero

Con la aplicación levantada, ejecutar:

```bash
curl -X POST http://localhost:8080/analisis-financiero \
  -H "Content-Type: application/json" \
  -d '{
    "ingreso_mensual": 1000,
    "ahorro_mensual": 200,
    "deuda_total": 1500,
    "pago_mensual_deudas": 150,
    "transacciones": [
      {
        "descripcion": "Supermercado",
        "categoria": "alimentacion",
        "monto": 120.50,
        "fecha": "2026-07-20",
        "tipo": "gastos"
      }
    ]
  }'
```

La respuesta devuelve una estructura estable con:

- `estado`
- `resumen`
- `indicadores`
- `recomendaciones`

Para probar el mismo endpoint desde Postman o Insomnia:

1. Crear una request `POST` a `http://localhost:8080/analisis-financiero`.
2. Agregar el header `Content-Type: application/json`.
3. En el body, seleccionar JSON y pegar el mismo ejemplo usado en el comando `curl`.
4. Enviar la request y verificar que la respuesta incluya `estado`, `resumen`, `indicadores` y `recomendaciones`.

### Validaciones del análisis financiero

El endpoint `POST /analisis-financiero` rechaza requests incompletos o inválidos con estado `400`.

Campos obligatorios:

- `ingreso_mensual`: mayor que cero.
- `ahorro_mensual`: mayor o igual que cero.
- `deuda_total`: mayor o igual que cero.
- `pago_mensual_deudas`: mayor o igual que cero.
- `transacciones`: lista obligatoria y no vacía.

Cada transacción debe incluir:

- `descripcion`
- `categoria`
- `monto`: mayor que cero.
- `fecha`
- `tipo`

Ejemplo de error:

```json
{
  "estado": "request_invalido",
  "errores": [
    {
      "campo": "transacciones[0].monto",
      "mensaje": "El monto de la transacción debe ser mayor que cero"
    }
  ]
}
```

### Ejecutar pruebas

```bash
cd backend
mvn test
```

---

## Colaboración

Antes de trabajar en el repositorio, revisa la [guía de contribución](CONTRIBUTING.md).

Ahí está definido el flujo del equipo: ramas por tarea, conventional commits, Pull Requests, Code Review y vinculación con Issues del sprint.
