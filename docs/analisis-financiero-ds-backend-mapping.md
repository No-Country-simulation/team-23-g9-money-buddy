# Mapeo Data Science Backend para `/analisis-financiero`

Este documento define el acuerdo vigente para el contrato oficial de `POST /analisis-financiero`. Backend es responsable de la validación HTTP, la orquestación, los cálculos determinísticos, el fallback seguro y el response HTTP final. El servicio FastAPI `ml-service` aporta predicción financiera y clasificación ML cuando corresponde.

## Alcance y fuente de verdad

| Área | Responsable | Fuente de verdad |
| --- | --- | --- |
| Endpoint público | Backend | `POST /analisis-financiero` |
| Validación del request HTTP | Backend | DTO/controller de Backend |
| Orquestación HTTP | Backend | Capa de servicio de Backend |
| Indicadores determinísticos | Backend | Payload del request actual únicamente |
| Clasificación de transacciones | Backend + `ml-service` | Reglas por descripción primero; ML solo para `otros` ambiguos |
| Perfil financiero | Backend + `ml-service` | Predicción financiera mapeada por Backend con fallback seguro |
| Response HTTP final | Backend | DTO/assembler de response de Backend |

El MVP es stateless. El endpoint no persiste requests, no lee deuda histórica y no calcula reducción real de deuda mes a mes.

## Responsabilidad sobre el request

| Campo | Nivel | Responsable | Regla |
| --- | --- | --- | --- |
| `credito_total` | Raíz | Backend valida, `ml-service` puede consumir | Obligatorio. Mayor o igual a cero. |
| `ingreso_mensual` | Raíz | Backend valida, `ml-service` puede consumir | Obligatorio. Mayor a cero. |
| `frecuencia_ahorro` | Raíz | Backend valida, `ml-service` puede consumir | Obligatorio. Valores permitidos: `NULA`, `BAJA`, `MEDIA`, `ALTA`. |
| `pago_mensual_deudas` | Raíz | Backend valida, `ml-service` puede consumir | Obligatorio. Mayor o igual a cero. |
| `transacciones` | Raíz | Backend valida, `ml-service` puede consumir | Obligatorio. Debe incluir al menos una transacción. |
| `tipo` | Transacción | Backend valida, `ml-service` puede consumir | Obligatorio. Valores permitidos: `Ingreso`, `Egreso`. |
| `fecha` | Transacción | Backend valida, `ml-service` puede consumir | Fecha obligatoria. Formato recomendado: `YYYY-MM-DD`. |
| `descripcion` | Transacción | Backend valida; reglas determinísticas consumen | Texto obligatorio no vacío. El modelo ML actual no lo usa como feature. |
| `tipo_pago` | Transacción | Backend valida | Campo público oficial. Obligatorio para `Egreso`. Valores permitidos: `Efectivo`, `Debito`, `Credito`. |
| `meses_a_deber` | Transacción | Backend valida | Obligatorio cuando `tipo_pago` es `Credito`. |
| `monto` | Transacción | Backend valida, `ml-service` puede consumir | Obligatorio. Mayor a cero. |

## Resolución de categoría de transacciones

La clasificación actual es híbrida y prioriza resultados explicables:

1. `Ingreso` siempre devuelve `categoria: "ingreso"` sin ML.
2. Para `Egreso`, se aplican reglas determinísticas sobre `descripcion`.
3. Si las reglas devuelven una categoría distinta de `otros`, Backend conserva esa categoría.
4. Si las reglas devuelven `otros`, Backend puede usar la categoría devuelta por `ml-service`.
5. Backend valida las categorías recibidas y cae al resultado determinístico si ML no está disponible, falla o devuelve una categoría inválida.

El modelo clasificador actual no usa `descripcion` como señal principal ni como feature. La descripción participa en las reglas determinísticas previas.

## Mapeo del clasificador de transacciones ML

| Dirección | Campo | Responsable | Notas |
| --- | --- | --- | --- |
| Input ML | `dia` | `ml-service` deriva desde `fecha` | Día de la transacción. |
| Input ML | `mes` | `ml-service` deriva desde `fecha` | Mes de la transacción. |
| Input ML | `anio` | `ml-service` deriva desde `fecha` | Año de la transacción. |
| Input ML | `valor_usd` | `ml-service` deriva desde `monto` | Usa `log1p(monto)`. |
| Input ML | `tipo_transaccion` | `ml-service` deriva desde `tipo` | Codifica `Ingreso` como `1`; otros casos como `0`. |
| Preprocesamiento | `scaler_categoria.pkl` | `ml-service` | Escala las features antes de llamar al RandomForest. |
| Output ML | `categoria` | `ml-service` devuelve a Backend | Backend acepta solo categorías oficiales; si no, usa fallback determinístico. |

Las categorías oficiales son `alimentos`, `transporte`, `salud`, `vivienda`, `educacion`, `ocio_entretenimiento`, `servicios`, `ropa_calzado`, `tecnologia`, `ingreso` y `otros`. En `resumen_gastos` y `porcentaje_categorias` solo deben aparecer categorías de egreso, por lo que `ingreso` queda excluida de esos objetos.

## Mapeo del perfil financiero ML

| Dirección | Campo | Responsable | Notas |
| --- | --- | --- | --- |
| Input | `credito_total` | Backend calcula/reenvía desde request | Valor del request actual. |
| Input | `ingreso_mensual` | Backend reenvía desde request | Valor del request actual. |
| Input | `frecuencia_ahorro` | Backend reenvía desde request | Valor del request actual. |
| Input | `deuda_total` | Backend calcula | Suma de egresos a crédito del request actual. |
| Input | `nivel_endeudamiento` | Backend calcula | `(deuda_total / credito_total) * 100`, manejando división por cero de forma segura. |
| Input | `pago_mensual_deudas` | Backend reenvía desde request | Valor del request actual. |
| Input | Transacciones clasificadas | Backend envía después de resolver categorías | Incluye `categoria` en cada transacción. |
| Input | Resumen de gastos, cuando aplique | Backend calcula desde transacciones clasificadas | Incluye totales y porcentajes por categoría disponibles en el contrato actual. |
| Output | `perfil_financiero` | Backend deriva desde la predicción/fallback | Backend lo ubica en `data.perfil_financiero`. |
| Output | `score_financiero` | `ml-service` predice; Backend valida/fallback | Backend lo ubica en `data.score_financiero`. |
| Output | `recomendaciones` | Backend deriva desde el análisis final | Backend lo ubica en `data.recomendaciones`. |

`ml-service` no devuelve el response HTTP completo. Backend arma el wrapper público y el objeto `data`.

## Cálculos determinísticos de Backend

| Campo | Responsable | Fórmula o regla |
| --- | --- | --- |
| `deuda_total` | Backend | Suma de transacciones del request actual donde `tipo` es `Egreso` y `tipo_pago` es `Credito`. |
| `nivel_endeudamiento` | Backend | `(deuda_total / credito_total) * 100`. Si `credito_total` es cero, Backend debe manejar la división de forma segura. |
| `gasto_total` | Backend | Suma de transacciones del request actual donde `tipo` es `Egreso`. |
| `resumen_gastos` | Backend | Suma de montos `Egreso` clasificados por categoría de gasto. |
| `porcentaje_categorias` | Backend | Participación por categoría según el contrato actual del response, usando egresos clasificados y el denominador acordado. |
| `ratio_pago_deudas` | Backend | `pago_mensual_deudas / ingreso_mensual`, manejando división por cero de forma segura. |
| `ratio_deuda_ingreso` | Backend | `deuda_total / ingreso_mensual`, manejando división por cero de forma segura. |

Estos cálculos usan únicamente el request que se está procesando. Backend en MVP no debe inferir deuda previa, deuda persistida ni amortización futura de deuda.

## Ensamblado del response final

| Campo del response | Responsable/fuente | Notas |
| --- | --- | --- |
| `success` | Backend | Indicador de éxito del wrapper público. |
| `message` | Backend | Mensaje del wrapper público. |
| `data` | Backend | Payload final del análisis armado por Backend. |
| `data.perfil_financiero` | Salida DS | Producido por el proceso/modelo de perfil DS e insertado por Backend. |
| `data.score_financiero` | Salida DS | Producido por el proceso/modelo de perfil DS e insertado por Backend. |
| `data.resumen_gastos` | Backend | Calculado desde transacciones de egreso clasificadas. |
| `data.indicadores.ingreso_mensual` | Backend | Eco/input de cálculo desde el request. |
| `data.indicadores.deuda_total` | Backend | Cálculo determinístico. |
| `data.indicadores.credito_total` | Backend | Eco/input de cálculo desde el request. |
| `data.indicadores.frecuencia_ahorro` | Backend | Eco/input de cálculo desde el request. |
| `data.indicadores.nivel_endeudamiento` | Backend | Cálculo determinístico. |
| `data.indicadores.pago_mensual_deudas` | Backend | Eco/input de cálculo desde el request. |
| `data.indicadores.gasto_total` | Backend | Cálculo determinístico. |
| `data.indicadores.ratio_pago_deudas` | Backend | Cálculo determinístico. |
| `data.indicadores.ratio_deuda_ingreso` | Backend | Cálculo determinístico. |
| `data.indicadores.porcentaje_*` | Backend | Cálculo determinístico desde categorías clasificadas. |
| `data.transacciones_clasificadas` | Backend + `ml-service` cuando aplica | Backend conserva campos del request y agrega `categoria` validada. |
| `data.recomendaciones` | Backend | Derivadas por Backend desde el análisis final. |

## Restricciones MVP

- `POST /analisis-financiero` no requiere persistencia en el MVP.
- No se lee ni se infiere deuda histórica.
- No se calcula reducción mensual real de deuda desde `meses_a_deber`.
- `ml-service` devuelve salidas de clasificador/predicción, no el response HTTP completo.
- Backend es responsable de la forma pública de la API y del ensamblado final del response.
- Backend conserva fallback determinístico si ML no está disponible o no es confiable.

## Checklist de compatibilidad

- [x] El campo público de tipo de pago es `tipo_pago`, no `tipoPago`.
- [x] Las transacciones `Ingreso` usan `categoria: "ingreso"`.
- [x] `nivel_endeudamiento = (deuda_total / credito_total) * 100`.
- [x] `deuda_total` sale únicamente de egresos a crédito del request actual.
- [x] El contrato es stateless para MVP: sin persistencia y sin deuda histórica.
- [x] El modelo clasificador actual usa `dia`, `mes`, `anio`, `valor_usd` y `tipo_transaccion`, no `descripcion`.
- [x] `descripcion` se usa en reglas determinísticas antes de consultar ML.
- [x] Backend valida categorías ML y mantiene fallback determinístico seguro.
- [x] El mapeo se alinea con el contrato Backend de la Issue #27 cuando aplica.
- [x] El mapeo se alinea con las definiciones DS de la Issue #26 cuando aplica.

## Ejemplo de request

```json
{
  "credito_total": 1000,
  "ingreso_mensual": 2000,
  "frecuencia_ahorro": "MEDIA",
  "pago_mensual_deudas": 120,
  "transacciones": [
    {
      "tipo": "Ingreso",
      "fecha": "2026-07-01",
      "descripcion": "Salario mensual",
      "monto": 2000
    },
    {
      "tipo": "Egreso",
      "fecha": "2026-07-03",
      "descripcion": "Supermercado",
      "tipo_pago": "Debito",
      "monto": 300
    },
    {
      "tipo": "Egreso",
      "fecha": "2026-07-08",
      "descripcion": "Compra de ropa",
      "tipo_pago": "Credito",
      "meses_a_deber": 5,
      "monto": 250
    }
  ]
}
```

## Ejemplo de response

```json
{
  "success": true,
  "message": "Análisis financiero generado exitosamente",
  "data": {
    "perfil_financiero": "estable",
    "score_financiero": 82,
    "resumen_gastos": {
      "alimentos": 300,
      "transporte": 0,
      "salud": 0,
      "vivienda": 0,
      "educacion": 0,
      "ocio_entretenimiento": 0,
      "servicios": 0,
      "ropa_calzado": 250,
      "tecnologia": 0,
      "otros": 0
    },
    "indicadores": {
      "ingreso_mensual": 2000,
      "deuda_total": 250,
      "credito_total": 1000,
      "nivel_endeudamiento": 25,
      "frecuencia_ahorro": "MEDIA",
      "pago_mensual_deudas": 120,
      "gasto_total": 550,
      "ratio_pago_deudas": 0.0600,
      "ratio_deuda_ingreso": 0.1250,
      "porcentaje_categorias": {
        "alimentos": 54.55,
        "transporte": 0,
        "salud": 0,
        "vivienda": 0,
        "educacion": 0,
        "ocio_entretenimiento": 0,
        "servicios": 0,
        "ropa_calzado": 45.45,
        "tecnologia": 0,
        "otros": 0
      }
    },
    "transacciones_clasificadas": [
      {
        "tipo": "Ingreso",
        "fecha": "2026-07-01",
        "descripcion": "Salario mensual",
        "tipo_pago": null,
        "meses_a_deber": null,
        "monto": 2000,
        "categoria": "ingreso"
      },
      {
        "tipo": "Egreso",
        "fecha": "2026-07-03",
        "descripcion": "Supermercado",
        "tipo_pago": "Debito",
        "meses_a_deber": null,
        "monto": 300,
        "categoria": "alimentos"
      },
      {
        "tipo": "Egreso",
        "fecha": "2026-07-08",
        "descripcion": "Compra de ropa",
        "tipo_pago": "Credito",
        "meses_a_deber": 5,
        "monto": 250,
        "categoria": "ropa_calzado"
      }
    ],
    "recomendaciones": [
      "Mantén protegido tu hábito de ahorro actual.",
      "Tu presión de deuda está controlada; sigue monitoreando los gastos de crédito."
    ]
  }
}
```
