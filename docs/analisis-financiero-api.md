# Contrato de API para `POST /analisis-financiero`

Este documento define el contrato público detallado del endpoint principal de Money Buddy. Incluye request, response, cálculos esperados, responsabilidades entre Backend y Data Science, validaciones y pasos de prueba manual.

## Resumen

| Punto | Definición |
| --- | --- |
| Método | `POST` |
| Ruta | `/analisis-financiero` |
| Content-Type | `application/json` |
| Persistencia | No aplica en MVP. El endpoint es stateless. |
| Response exitoso | Wrapper con `success`, `message` y `data`. |
| Error de validación | HTTP `400` con formato de error consistente. |

## Request

El request recibe datos financieros del usuario y una lista de transacciones.

El request público no debe incluir `deuda_total` ni `nivel_endeudamiento`. Backend calcula ambos valores a partir de los datos recibidos en el request actual.

### Campos raíz

| Campo | Tipo esperado | Regla |
| --- | --- | --- |
| `credito_total` | número | Obligatorio. Mayor o igual que cero. |
| `ingreso_mensual` | número | Obligatorio. Mayor que cero. |
| `frecuencia_ahorro` | texto | Obligatorio. Valores: `NULA`, `BAJA`, `MEDIA`, `ALTA`. |
| `pago_mensual_deudas` | número | Obligatorio. Mayor o igual que cero. |
| `transacciones` | lista | Obligatoria. Debe incluir al menos una transacción. |

### Campos por transacción

| Campo | Tipo esperado | Regla |
| --- | --- | --- |
| `tipo` | texto | Obligatorio. Valores: `Ingreso`, `Egreso`. |
| `fecha` | fecha | Obligatoria. Formato recomendado: `YYYY-MM-DD`. |
| `descripcion` | texto | Obligatoria. No debe estar vacía. |
| `tipo_pago` | texto | Condicional. Valores: `Efectivo`, `Debito`, `Credito`. Obligatorio cuando `tipo` es `Egreso`. |
| `meses_a_deber` | número entero | Condicional. Aplica cuando `tipo_pago` es `Credito`. |
| `monto` | número | Obligatorio. Mayor que cero. |

### Ejemplo curl

Con la aplicación levantada, ejecutar:

```bash
curl -X POST http://localhost:8080/analisis-financiero \
  -H "Content-Type: application/json" \
  -d '{
    "credito_total": 1500,
    "ingreso_mensual": 1000,
    "frecuencia_ahorro": "MEDIA",
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

## Response

La respuesta oficial usa un wrapper con:

| Campo | Descripción |
| --- | --- |
| `success` | Indica si la operación fue exitosa. |
| `message` | Mensaje general de la operación. |
| `data` | Objeto con el resultado del análisis financiero. |

### `data`

Dentro de `data`, la respuesta incluye:

| Campo | Descripción |
| --- | --- |
| `perfil_financiero` | Perfil financiero calculado para el usuario. |
| `score_financiero` | Puntaje financiero general. |
| `resumen_gastos` | Totales agrupados por categoría de gasto. |
| `indicadores` | Métricas financieras calculadas a partir del request. |
| `transacciones_clasificadas` | Transacciones procesadas y clasificadas para el análisis. |
| `recomendaciones` | Recomendaciones financieras generadas para el usuario. |

### `resumen_gastos`

Incluye gasto total por categoría de egreso:

- `alimentos`
- `transporte`
- `salud`
- `vivienda`
- `educacion`
- `ocio_entretenimiento`
- `servicios`
- `ropa_calzado`
- `tecnologia`
- `otros`

### `indicadores`

El documento actualizado define campos como:

- `ingreso_mensual`
- `deuda_total`
- `credito_total`
- `frecuencia_ahorro`
- `nivel_endeudamiento`
- `pago_mensual_deudas`
- `gasto_total`
- `ratio_pago_deudas`
- `ratio_deuda_ingreso`
- porcentajes por categoría de egreso: `alimentos`, `transporte`, `salud`, `vivienda`, `educacion`, `ocio_entretenimiento`, `servicios`, `ropa_calzado`, `tecnologia` y `otros`.

### `transacciones_clasificadas`

Toda transacción debe incluir `categoria`:

- Si `tipo` es `Egreso`, `categoria` corresponde a la categoría de gasto clasificada.
- Si `tipo` es `Ingreso`, `categoria` debe ser `ingreso`.

### Ejemplo parcial de response

```json
{
  "success": true,
  "message": "Análisis financiero generado exitosamente",
  "data": {
    "perfil_financiero": "estable",
    "score_financiero": 72,
    "resumen_gastos": {
      "alimentos": 120.50,
      "transporte": 50.25,
      "salud": 0,
      "vivienda": 0,
      "educacion": 0,
      "ocio_entretenimiento": 0,
      "servicios": 300,
      "ropa_calzado": 0,
      "tecnologia": 0,
      "otros": 0
    },
    "indicadores": {
      "ingreso_mensual": 1000,
      "deuda_total": 300,
      "credito_total": 1500,
      "nivel_endeudamiento": 20,
      "frecuencia_ahorro": "MEDIA",
      "pago_mensual_deudas": 150,
      "gasto_total": 470.75,
      "ratio_pago_deudas": 0.1500,
      "ratio_deuda_ingreso": 0.3000,
      "porcentaje_categorias": {
        "alimentos": 25.60,
        "transporte": 10.67,
        "salud": 0,
        "vivienda": 0,
        "educacion": 0,
        "ocio_entretenimiento": 0,
        "servicios": 63.73,
        "ropa_calzado": 0,
        "tecnologia": 0,
        "otros": 0
      }
    },
    "transacciones_clasificadas": [
      {
        "tipo": "Egreso",
        "fecha": "2026-07-20",
        "descripcion": "Supermercado",
        "tipo_pago": "Debito",
        "meses_a_deber": null,
        "monto": 120.50,
        "categoria": "alimentos"
      },
      {
        "tipo": "Egreso",
        "fecha": "2026-07-21",
        "descripcion": "Transporte publico",
        "tipo_pago": "Efectivo",
        "meses_a_deber": null,
        "monto": 50.25,
        "categoria": "transporte"
      },
      {
        "tipo": "Ingreso",
        "fecha": "2026-07-01",
        "descripcion": "Salario",
        "tipo_pago": null,
        "meses_a_deber": null,
        "monto": 1000,
        "categoria": "ingreso"
      },
      {
        "tipo": "Egreso",
        "fecha": "2026-07-10",
        "descripcion": "Gas",
        "tipo_pago": "Credito",
        "meses_a_deber": 3,
        "monto": 300,
        "categoria": "servicios"
      }
    ],
    "recomendaciones": [
      "Mantén protegido tu hábito de ahorro actual.",
      "Tu presión de deuda está controlada; sigue monitoreando los gastos de crédito."
    ]
  }
}
```

## Cálculos backend

El endpoint es stateless: calcula el resultado usando únicamente el request actual, sin persistencia ni base de datos.

Backend calcula y normaliza:

| Resultado | Regla |
| --- | --- |
| `transacciones_clasificadas` | Normaliza `tipo` como `Ingreso` o `Egreso`, conserva los datos de la transacción y asigna `categoria`. Los ingresos usan `ingreso`; los egresos se clasifican por palabras clave de la descripción. |
| `deuda_total` | Suma de transacciones `Egreso` con `tipo_pago` igual a `Credito` dentro del request actual. |
| `gasto_total` | Suma de transacciones `Egreso`. |
| `resumen_gastos` | Totales por categoría de egreso usando las transacciones clasificadas. Incluye siempre las categorías soportadas, aunque su total sea cero. |
| `porcentaje_categorias` | Porcentaje de cada categoría respecto al `gasto_total`. Si el divisor es cero, responde `0.00`. |
| `nivel_endeudamiento` | `(deuda_total / credito_total) * 100`. Si `credito_total` es cero, responde `0.00`. |
| `ratio_pago_deudas` | `pago_mensual_deudas / ingreso_mensual`. Si `ingreso_mensual` es cero, responde `0.0000`. |
| `ratio_deuda_ingreso` | `deuda_total / ingreso_mensual`. Si `ingreso_mensual` es cero, responde `0.0000`. |
| `score_financiero` | Parte de un puntaje base según `frecuencia_ahorro`: `ALTA` = 85, `MEDIA` = 72, `BAJA` = 58, `NULA` u otro valor normalizado = 42. Resta puntos por presión de deuda y limita el resultado entre 0 y 100. |
| `perfil_financiero` | Devuelve `estable`, `requiere_atencion` o `riesgo_alto` según `score_financiero`, `ratio_pago_deudas` y `nivel_endeudamiento`. |
| `recomendaciones` | Genera mensajes según frecuencia de ahorro, presión de deuda y perfil financiero. |

Las categorías de egreso soportadas son:

- `alimentos`
- `transporte`
- `salud`
- `vivienda`
- `educacion`
- `ocio_entretenimiento`
- `servicios`
- `ropa_calzado`
- `tecnologia`
- `otros`

La clasificación actual se basa en palabras clave de la descripción. Si un egreso no coincide con ninguna regla específica, se clasifica como `otros`.

## Responsabilidad Data Science vs Backend

Data Science no devuelve el response HTTP completo.

| Área | Responsable |
| --- | --- |
| Modelo o proceso de transacciones | Devuelve categorías para transacciones. |
| Modelo o proceso de perfil financiero | Devuelve `perfil_financiero`, `score_financiero` y `recomendaciones`. |
| Backend | Calcula indicadores determinísticos, orquesta los resultados y arma el response HTTP final. |

La tabla detallada de mapeo entre Data Science y Backend vive en [`analisis-financiero-ds-backend-mapping.md`](analisis-financiero-ds-backend-mapping.md).

## Validaciones esperadas

El endpoint `POST /analisis-financiero` rechaza requests incompletos o inválidos con estado `400`.

### Campos raíz obligatorios

- `credito_total`: mayor o igual que cero.
- `ingreso_mensual`: mayor que cero.
- `frecuencia_ahorro`: debe ser `NULA`, `BAJA`, `MEDIA` o `ALTA`.
- `pago_mensual_deudas`: mayor o igual que cero.
- `transacciones`: lista obligatoria y no vacía.

### Campos obligatorios por transacción

- `tipo`: debe ser `Ingreso` o `Egreso`.
- `fecha`: fecha de la transacción.
- `descripcion`: descripción de la transacción.
- `tipo_pago`: si está presente, debe ser `Efectivo`, `Debito` o `Credito`; es obligatorio cuando `tipo` es `Egreso`.
- `meses_a_deber`: obligatorio cuando `tipo_pago` es `Credito`.
- `monto`: mayor que cero.

### Formato de error

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

## Cómo probar desde Postman o Insomnia

1. Crear una request `POST` a `http://localhost:8080/analisis-financiero`.
2. Agregar el header `Content-Type: application/json`.
3. En el body, seleccionar JSON y pegar el mismo ejemplo usado en el comando `curl`.
4. Enviar la request y verificar que la respuesta incluya `success`, `message` y `data`.

## Pruebas automatizadas relacionadas

Las pruebas automatizadas en `AnalisisFinancieroControllerTest` deben cubrir el contrato oficial y garantizar el correcto funcionamiento de las validaciones:

Casos válidos:

- Envío de datos completos, incluyendo flujo de caja estable y cálculo de indicadores.
- Envío de datos mínimos válidos, verificando la estructura completa de la respuesta: `success`, `message` y `data`.

Casos inválidos con HTTP `400` y formato de error consistente:

- Omitir campo financiero obligatorio, por ejemplo sin `ingreso_mensual`.
- Omitir múltiples campos financieros obligatorios simultáneamente.
- Valor de ingreso mensual inválido, por ejemplo `ingreso_mensual` en cero o negativo.
- Transacción incompleta, con omisión de campos obligatorios como `tipo`, `fecha`, `descripcion`, `tipo_pago` y `monto`.
- Transacción con datos inválidos, como monto negativo, descripción vacía, fecha nula o enum inválido.
- Transacción con `tipo_pago` inválido, incluso cuando `tipo` es `Ingreso`.
- Transacción con `tipo_pago: "Credito"` sin `meses_a_deber`.
- Lista de transacciones vacía: `transacciones: []`.
- Cuerpo de solicitud vacío o con JSON mal formado.

Para ejecutar las pruebas del backend:

```bash
cd backend
mvn test
```
