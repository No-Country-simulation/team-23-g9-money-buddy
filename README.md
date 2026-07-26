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

### Contrato `POST /analisis-financiero`

#### Request

#### Campos raíz

| Campo | Tipo esperado | Regla |
| --- | --- | --- |
| `credito_total` | número | Obligatorio. Mayor o igual que cero. |
| `ingreso_mensual` | número | Obligatorio. Mayor que cero. |
| `frecuencia_ahorro` | texto | Obligatorio. Valores: `NULA`, `BAJA`, `MEDIA`, `ALTA`. |
| `nivel_endeudamiento` | número | Obligatorio. Valor entre 0 y 100, inclusive. |
| `pago_mensual_deudas` | número | Obligatorio. Mayor o igual que cero. |
| `transacciones` | lista | Obligatoria. Debe incluir al menos una transacción. |

#### Campos por transacción

| Campo | Tipo esperado | Regla |
| --- | --- | --- |
| `tipo` | texto | Obligatorio. Valores: `Ingreso`, `Egreso`. |
| `fecha` | fecha | Obligatoria. Formato recomendado: `YYYY-MM-DD`. |
| `descripcion` | texto | Obligatoria. No debe estar vacía. |
| `tipo_pago` | texto | Obligatorio. Valores: `Efectivo`, `Debito`, `Credito`. |
| `meses_a_deber` | número entero | Condicional. Aplica cuando `tipo_pago` es `Credito`. |
| `monto` | número | Obligatorio. Mayor que cero. |

#### Ejemplo de request válido

Con la aplicación levantada, ejecutar:

```bash
curl -X POST http://localhost:8080/analisis-financiero \
  -H "Content-Type: application/json" \
  -d '{
    "credito_total": 1500,
    "ingreso_mensual": 1000,
    "frecuencia_ahorro": "MEDIA",
    "nivel_endeudamiento": 25,
    "pago_mensual_deudas": 150,
    "transacciones": [
      {
        "tipo": "Egreso",
        "fecha": "2026-07-20",
        "descripcion": "Supermercado",
        "tipo_pago": "Debito",
        "monto": 120.50
      },
      {
        "tipo": "Egreso",
        "fecha": "2026-07-21",
        "descripcion": "Transporte publico",
        "tipo_pago": "Efectivo",
        "monto": 50.25
      },
      {
        "tipo": "Ingreso",
        "fecha": "2026-07-01",
        "descripcion": "Salario",
        "tipo_pago": "Efectivo",
        "monto": 1000
      },
      {
        "tipo": "Egreso",
        "fecha": "2026-07-10",
        "descripcion": "Gas",
        "tipo_pago": "Credito",
        "meses_a_deber": 3,
        "monto": 300
      }
    ]
  }'
```

#### Response

La respuesta oficial usa un wrapper con:

- `success`: indica si la operación fue exitosa.
- `message`: mensaje general de la operación.
- `data`: objeto con el resultado del análisis financiero.

Dentro de `data`, la respuesta incluye:

| Campo | Descripción |
| --- | --- |
| `perfil_financiero` | Perfil financiero calculado para el usuario. |
| `score_financiero` | Puntaje financiero general. |
| `resumen_gastos` | Totales agrupados por categoría de gasto. |
| `indicadores` | Métricas financieras calculadas a partir del request. |
| `transacciones_clasificadas` | Transacciones procesadas y clasificadas para el análisis. |
| `recomendaciones` | Recomendaciones financieras generadas para el usuario. |

Dentro de `resumen_gastos`:

- gasto total por categoría: `alimentacion`, `transporte`, `salud`, `vivienda`, `educacion`, `ocio_entretenimiento`, `servicios`, `compras` y `otros`.

Dentro de `indicadores`, el documento actualizado define campos como:

- `ingreso_mensual`
- `deuda_total`
- `credito_total`
- `frecuencia_ahorro`
- `nivel_endeudamiento`
- `pago_mensual_deudas`
- `gasto_total`
- `ratio_pago_deudas`
- `ratio_deuda_ingreso`
- porcentajes por categoría: `alimentacion`, `transporte`, `salud`, `vivienda`, `educacion`, `ocio_entretenimiento`, `servicios`, `compras` y `otros`.

Ejemplo parcial de response:

```json
{
  "success": true,
  "message": "Análisis financiero generado correctamente",
  "data": {
    "perfil_financiero": "CONTROLADO",
    "score_financiero": 78,
    "resumen_gastos": {
      "alimentacion": 120.50,
      "transporte": 50.25,
      "salud": 0,
      "vivienda": 0,
      "educacion": 0,
      "ocio_entretenimiento": 0,
      "servicios": 0,
      "compras": 0,
      "otros": 300
    },
    "indicadores": {
      "ingreso_mensual": 1000,
      "deuda_total": 1500,
      "credito_total": 1500,
      "frecuencia_ahorro": "MEDIA",
      "nivel_endeudamiento": 30,
      "pago_mensual_deudas": 150,
      "gasto_total": 470.75,
      "ratio_pago_deudas": 0.1500,
      "ratio_deuda_ingreso": 1.5000,
      "porcentaje_alimentacion": 12.05,
      "porcentaje_transporte": 5.03,
      "porcentaje_salud": 0,
      "porcentaje_vivienda": 0,
      "porcentaje_educacion": 0,
      "porcentaje_ocio_entretenimiento": 0,
      "porcentaje_servicios": 0,
      "porcentaje_compras": 0,
      "porcentaje_otros": 30
    },
    "transacciones_clasificadas": [
      {
        "tipo": "Egreso",
        "fecha": "2026-07-02",
        "descripcion": "Gas",
        "tipo_pago": "Credito",
        "meses_a_deber": 1,
        "monto": 420,
        "categoria": "servicios"
      }
    ],
    "recomendaciones": [
      "Mantener el nivel de ahorro mensual y revisar gastos recurrentes.",
      "Monitorear el pago mensual de deudas para evitar presión financiera."
    ]
  }
}
```

Para probar el mismo endpoint desde Postman o Insomnia:

1. Crear una request `POST` a `http://localhost:8080/analisis-financiero`.
2. Agregar el header `Content-Type: application/json`.
3. En el body, seleccionar JSON y pegar el mismo ejemplo usado en el comando `curl`.
4. Enviar la request y verificar que la respuesta incluya `success`, `message` y `data`.

### Validaciones esperadas del análisis financiero

El endpoint `POST /analisis-financiero` rechaza requests incompletos o inválidos con estado `400`.

Campos obligatorios:

- `credito_total`: mayor o igual que cero.
- `ingreso_mensual`: mayor que cero.
- `frecuencia_ahorro`: debe ser `NULA`, `BAJA`, `MEDIA` o `ALTA`.
- `nivel_endeudamiento`: mayor o igual que 0 y menor o igual que 100.
- `pago_mensual_deudas`: mayor o igual que cero.
- `transacciones`: lista obligatoria y no vacía.

Cada transacción debe incluir:

- `tipo`: debe ser `Ingreso` o `Egreso`.
- `fecha`: fecha de la transacción.
- `descripcion`: descripción de la transacción.
- `tipo_pago`: debe ser `Efectivo`, `Debito` o `Credito`.
- `meses_a_deber`: obligatorio cuando `tipo_pago` es `Credito`.
- `monto`: mayor que cero.

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

Para ejecutar las pruebas unitarias y de integración del backend:

```bash
cd backend
mvn test
```

#### Pruebas de validación del endpoint (`POST /analisis-financiero`)

Las pruebas automatizadas en `AnalisisFinancieroControllerTest` deben cubrir el contrato oficial y garantizar el correcto funcionamiento de las validaciones:

* **Casos Válidos:**
  * Envío de datos completos (flujo de caja estable y cálculo de indicadores).
  * Envío de datos mínimos válidos (verificando la estructura completa de la respuesta: `success`, `message` y `data`).
* **Casos Inválidos (Retorno con HTTP 400 y formato de error consistente):**
  * Omitir campo financiero obligatorio (ej. sin `ingreso_mensual`).
  * Omitir múltiples campos financieros obligatorios simultáneamente.
  * Valor de ingreso mensual inválido (ej. `ingreso_mensual` en cero o negativo).
  * Transacción incompleta (omisión de campos obligatorios como `tipo`, `fecha`, `descripcion`, `tipo_pago` y `monto`).
  * Transacción con datos inválidos (monto negativo, descripción vacía, fecha nula o enum inválido).
  * Transacción con `tipo_pago: "Credito"` sin `meses_a_deber`.
  * Lista de transacciones vacía (`transacciones: []`).
  * Cuerpo de solicitud vacío o con JSON mal formado.


---

## Colaboración

Antes de trabajar en el repositorio, revisa la [guía de contribución](CONTRIBUTING.md).

Ahí está definido el flujo del equipo: ramas por tarea, conventional commits, Pull Requests, Code Review y vinculación con Issues del sprint.
