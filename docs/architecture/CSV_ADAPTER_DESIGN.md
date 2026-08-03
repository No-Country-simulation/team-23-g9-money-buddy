# Diseño de Arquitectura: Adaptador CSV/Excel Secundario

**Issue Relacionado:** #11
**Área:** Backend / Arquitectura
**Estado:** Propuesta de Diseño (Sprint 1)

## 1. Contexto y Objetivo
La estrategia de entrada de datos para MoneyBuddy es híbrida. El flujo principal de comunicación está basado en JSON para garantizar un contrato API estándar. El objetivo de este diseño es definir un adaptador secundario que permita la carga masiva de transacciones vía archivos CSV/Excel, sin alterar, bloquear ni interferir con el flujo principal JSON.

## 2. Alcance y Criterios del Sprint 1
*   **Diseño:** Se define la plantilla, transformaciones y flujo arquitectónico (este documento).
*   **Implementación:** Acorde a la priorización del Sprint 1, la *implementación en código* de este adaptador queda fuera del alcance inicial y se programará para sprints posteriores, priorizando la estabilidad del MVP JSON.

## 3. Plantilla y Formato Soportado
Para la primera iteración, el sistema soportará archivos **CSV (delimitados por comas)** con una estructura de cabeceras estricta. 

Las columnas esperadas se mapearán directamente al contrato actualizado definido en las issues **#26** y **#29**. **Importante:** No se solicitarán datos calculados por el sistema (como `ahorro_mensual` o `deuda_total`) como entrada pública.

| Columna | Obligatorio | Valores Esperados / Reglas |
| :--- | :---: | :--- |
| `tipo` | Sí | `Ingreso` o `Egreso` (Insensible a mayúsculas/minúsculas). |
| `fecha` | Sí | Formato `YYYY-MM-DD`. |
| `descripcion` | Sí | Texto libre, máximo 200 caracteres. |
| `tipo_pago` | Condicional | `Efectivo`, `Debito`, `Credito`. Obligatorio si `tipo` es `Egreso`. |
| `meses_a_deber`| Condicional | Número entero. Obligatorio si `tipo_pago` es `Credito`. |
| `monto` | Sí | Número decimal positivo, mayor a cero. |

## 4. Flujo de Transformación y Reutilización
El principio fundamental de este adaptador es **cero duplicación de lógica financiera**. El adaptador actuará únicamente como una capa de traducción:

1.  **Recepción:** Un nuevo endpoint (ej. `POST /api/v1/transacciones/upload`) recibirá el archivo usando `MultipartFile`.
2.  **Parseo y Transformación:** 
    *   `fecha`: Se transformará de `String` a `LocalDate` (ISO-8601).
    *   `monto`: Se parseará a `BigDecimal`.
    *   `meses_a_deber`: Se parseará a `Integer`.
3.  **Inyección en Flujo Principal:** Las filas transformadas se empaquetarán en los *records* existentes (`TransaccionRequest`). Estas instancias se pasarán directamente al servicio principal actual (`TransaccionesService` o `AnalisisFinancieroService`).

## 5. Manejo de Errores Esperados
El adaptador deberá capturar y responder de forma estandarizada (usando nuestro `GlobalExceptionHandler`) ante los siguientes escenarios:

*   **`ERR_ARCHIVO_VACIO`:** El archivo enviado no contiene registros o pesa 0 bytes.
*   **`ERR_FORMATO_INVALIDO`:** El archivo no tiene extensión .csv o faltan columnas obligatorias en la cabecera.
*   **`ERR_PARSE_TIPO_DATO`:** Una columna contiene un tipo de dato incorrecto (ej. letras en la columna `monto` o formato de fecha inválido).
*   **`ERR_VALIDACION_NEGOCIO`:** El archivo se parseó bien, pero falló la validación de negocio existente (ej. monto negativo o egreso sin tipo de pago). Se retornará la línea exacta que falló.