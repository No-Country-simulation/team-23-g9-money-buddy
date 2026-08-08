# Estrategia de carga de modelos Data Science

Este documento define el acuerdo operativo para cargar los artefactos de Data Science sin bloquear la integración de Backend ni la demo del MVP. La decisión de mantener un fallback determinístico en Backend y preparar dos rutas de carga: archivo local para desarrollo y OCI Object Storage privado para demo.

## Decisión rápida

| Punto | Decisión |
| --- | --- |
| Procesos DS | Dos artefactos lógicos: clasificador de transacciones y perfil financiero. |
| Desarrollo local | Usar archivos locales fuera de Git cuando DS entregue artefactos ejecutables. |
| Demo | Usar OCI Object Storage privado como fuente de artefactos. |
| Fallback Sprint 1 | Backend puede seguir usando reglas determinísticas si los artefactos no están disponibles. |
| Contrato HTTP | Backend conserva el contrato público de `POST /analisis-financiero` y arma el response final. |
| Secretos | No se commitean credenciales, llaves, URLs firmadas ni configuración OCI local. |

## Quick path

1. Data Science entrega los artefactos acordados con nombres estables.
2. Backend los prueba localmente desde una ruta no versionada.
3. Si el artefacto no está disponible, Backend mantiene el fallback determinístico actual.
4. Para demo, el owner OCI sube los artefactos al bucket privado y registra evidencia segura.
5. Backend configura la estrategia de carga por variables de entorno, no por valores hardcodeados.

## Artefactos acordados

| Artefacto lógico | Nombre recomendado | Responsable | Uso |
| --- | --- | --- | --- |
| Clasificador de transacciones | `transaction-classifier.<ext>` | Data Science | Recibe transacciones normalizadas y devuelve `categoria`. |
| Perfil financiero | `financial-profile.<ext>` | Data Science | Recibe indicadores y transacciones clasificadas; devuelve `perfil_financiero`, `score_financiero` y `recomendaciones`. |

Extensiones permitidas para MVP: `.pkl`, `.joblib`, `.onnx` o `.zip`, según lo que Data Science confirme como ejecutable por el runtime elegido.

## Estrategias de carga

| Estrategia | Cuándo usarla | Comportamiento esperado |
| --- | --- | --- |
| `deterministic` | Sprint 1 sin artefactos listos, pruebas locales o fallback seguro. | Backend usa las reglas internas actuales y no intenta cargar modelos externos. |
| `local` | Desarrollo local Backend/DS. | Backend lee los artefactos desde rutas locales no versionadas. |
| `oci` | Demo o integración compartida. | Backend obtiene los artefactos desde OCI Object Storage privado usando configuración segura del entorno. |

La estrategia por defecto recomendada es `deterministic`, porque evita bloquear el endpoint JSON mientras se termina la entrega DS.

## Configuración propuesta

Estas propuestas de variables documentan el contrato de configuración. No deben agregarse con valores reales al repositorio.

```env
MODEL_LOADING_STRATEGY=deterministic
TRANSACTION_CLASSIFIER_PATH=backend/models/transaction-classifier.joblib
FINANCIAL_PROFILE_PATH=backend/models/financial-profile.joblib
OCI_MODEL_BUCKET=money-buddy-artifacts-free-tier
OCI_TRANSACTION_CLASSIFIER_OBJECT=models/transaction-classifier.joblib
OCI_FINANCIAL_PROFILE_OBJECT=models/financial-profile.joblib
```

Reglas:

- `MODEL_LOADING_STRATEGY` acepta `deterministic`, `local` u `oci`.
- Las rutas `*_PATH` aplican solo para `local`.
- Los valores `OCI_*` aplican solo para `oci`.
- Credenciales OCI deben venir del entorno seguro de ejecución, nunca de archivos versionados.

## Contrato de integración Backend ↔ DS

Backend no espera que Data Science devuelva el response HTTP completo. Data Science entrega salidas específicas y Backend ensambla el contrato final documentado en [`analisis-financiero-ds-backend-mapping.md`](analisis-financiero-ds-backend-mapping.md).

| Proceso | Input principal | Output DS | Ensamblado Backend |
| --- | --- | --- | --- |
| Clasificador de transacciones | `tipo`, `fecha`, `descripcion`, `tipo_pago`, `meses_a_deber`, `monto` | `categoria` | Agrega `categoria` a `data.transacciones_clasificadas`. |
| Perfil financiero | Indicadores calculados, datos raíz y transacciones clasificadas | `perfil_financiero`, `score_financiero`, `recomendaciones` | Inserta esos campos dentro de `data`. |

## Manejo de fallos

| Caso | Respuesta esperada |
| --- | --- |
| Falta artefacto local | Registrar el problema y usar fallback determinístico si la estrategia lo permite. |
| OCI no disponible en demo | No exponer secretos en logs; usar fallback o detener despliegue según decisión del equipo. |
| Formato de artefacto incompatible | Mantener contrato HTTP estable y coordinar corrección con DS. |
| Salida DS incompleta | Backend no debe romper el wrapper público; debe validar/manejar la salida antes de responder. |

## Seguridad OCI

Para demo, seguir la guía de [`oci-object-storage-artefactos.md`](oci-object-storage-artefactos.md):

- bucket privado;
- sin credenciales en Git;
- sin URLs firmadas en issues, PRs o logs;
- evidencia segura sin identificadores sensibles;
- archivos sin datos financieros reales de usuarios.

## Checklist para cerrar la Issue #19

- [x] Estrategia local definida: archivos locales no versionados con fallback determinístico.
- [x] Estrategia demo definida: OCI Object Storage privado.
- [x] Nombres recomendados de artefactos definidos.
- [x] Variables de configuración propuestas.
- [x] Contrato alineado con el mapeo Backend ↔ DS de la Issue #29.
- [ ] Data Science confirma extensión/formato final de cada artefacto.
- [ ] Owner OCI confirma bucket y upload de prueba con evidencia segura.
- [ ] Backend implementa la carga real cuando exista artefacto ejecutable.

## Fuera de alcance

- Entrenar modelos nuevos.
- Implementar persistencia.
- Guardar artefactos o modelos en Git.
- Agregar credenciales OCI al repositorio.
- Cambiar el contrato público de `POST /analisis-financiero`.
