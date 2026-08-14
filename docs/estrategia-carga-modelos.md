# Estrategia de carga de modelos Data Science

La integración de modelos ya está implementada con un servicio FastAPI `ml-service`, adaptadores HTTP en Backend y fallback determinístico cuando ML no está disponible o devuelve una salida inválida.

## Decisión rápida

| Punto | Estado actual |
| --- | --- |
| Runtime ML | FastAPI `ml-service` expone `/health`, `/predict` y `/classify-transactions`. |
| Integración Backend | Backend consume `ml-service` por HTTP y conserva el contrato público de `POST /analisis-financiero`. |
| Desarrollo local | Los artefactos se cargan desde `ml/models/` o desde rutas configuradas por entorno. |
| Demo | OCI Object Storage privado puede descargar artefactos al iniciar `ml-service`. |
| Descarga OCI | Implementada y deshabilitada por defecto con `OCI_MODEL_DOWNLOAD_ENABLED=false`. |
| Fallback seguro | Backend mantiene reglas determinísticas si ML falla, no responde o devuelve datos inválidos. |
| Secretos | No se commitean credenciales, llaves, URLs firmadas ni configuración OCI local. |

## Quick path

1. Colocar artefactos locales no versionados en `ml/models/` o configurar rutas por variables de entorno.
2. Levantar la aplicación con Docker Compose o iniciar `ml-service` localmente.
3. Verificar `GET http://localhost:8000/health`.
4. Backend llama a `ml-service` para score financiero y clasificación de transacciones.
5. Si ML no responde correctamente, Backend conserva la respuesta pública usando fallback determinístico.

## Artefactos actuales

| Artefacto lógico | Nombre actual | Uso |
| --- | --- | --- |
| Modelo de estabilidad financiera | `modelo_financial_stability.pkl` | `/predict` devuelve la predicción usada por Backend. |
| Clasificador de transacciones | `classify-transactions.pkl` | Clasifica egresos ambiguos cuando las reglas determinísticas devuelven `otros`. |
| Label encoder de categoría | `label_encoder_categoria.pkl` | Traduce la salida numérica del clasificador a categoría textual. |
| Scaler de categoría | `scaler_categoria.pkl` | Escala features antes de la predicción RandomForest. |

Los artefactos son binarios y no se versionan en Git.

## Estrategias de carga

| Estrategia | Cuándo usarla | Comportamiento esperado |
| --- | --- | --- |
| Determinística | Fallback seguro o salida ML inválida/no disponible. | Backend usa reglas internas y mantiene estable el contrato HTTP. |
| Local | Desarrollo local Backend/DS. | `ml-service` carga artefactos desde `ml/models/` o rutas configuradas. |
| OCI | Demo o integración compartida. | `ml-service` descarga artefactos desde OCI Object Storage privado si `OCI_MODEL_DOWNLOAD_ENABLED=true`. |

La descarga OCI está deshabilitada por defecto para que el entorno local no dependa de credenciales ni red externa.

## Configuración

Estas variables documentan la configuración actual. No deben agregarse con valores reales al repositorio.

```env
MODEL_PATH=./models/modelo_financial_stability.pkl
CLASSIFIER_MODEL_PATH=./models/classify-transactions.pkl
LABEL_ENCODER_CATEGORIA_PATH=./models/label_encoder_categoria.pkl
SCALER_CATEGORIA_PATH=./models/scaler_categoria.pkl
OCI_MODEL_DOWNLOAD_ENABLED=false
OCI_MODEL_FORCE_DOWNLOAD=false
OCI_CONFIG_FILE=~/.oci/config
OCI_CONFIG_PROFILE=DEFAULT
OCI_NAMESPACE=<namespace>
OCI_BUCKET_NAME=<bucket-name>
OCI_FINANCIAL_STABILITY_OBJECT=financial-modelsmodelo_financial_stability.pkl
OCI_TRANSACTION_CLASSIFIER_OBJECT=financial-modelsrandom_forest_model (champion).pkl
OCI_LABEL_ENCODER_CATEGORIA_OBJECT=financial-modelslabel_encoder_categoria.pkl
OCI_SCALER_CATEGORIA_OBJECT=financial-modelsscaler_categoria.pkl
ML_SERVICE_BASE_URL=http://ml-service:8000
```

Reglas:

- Las rutas `*_PATH` aplican a carga local del `ml-service`.
- `OCI_MODEL_DOWNLOAD_ENABLED=false` evita descargas por defecto.
- Los valores `OCI_*` aplican solo si la descarga OCI está habilitada.
- `ML_SERVICE_BASE_URL` configura el endpoint interno que usa Backend.
- Credenciales OCI deben venir del entorno seguro de ejecución, nunca de archivos versionados.

## Contrato de integración Backend ↔ ML

Backend no espera que `ml-service` devuelva el response HTTP completo. El servicio ML entrega salidas específicas y Backend ensambla el contrato final documentado en [`analisis-financiero-ds-backend-mapping.md`](analisis-financiero-ds-backend-mapping.md).

| Proceso | Input principal | Output ML | Ensamblado Backend |
| --- | --- | --- | --- |
| Clasificador de transacciones | Transacciones compatibles con Backend | `categoria` por transacción | Agrega `categoria` a `data.transacciones_clasificadas`; valida salida y cae a reglas si hace falta. |
| Score financiero | Payload financiero compatible con Backend | Predicción del modelo | Backend usa la predicción para el análisis final y mantiene fallback determinístico ante errores. |

## Manejo de fallos

| Caso | Respuesta esperada |
| --- | --- |
| Falta artefacto local | `ml-service` expone estado degradado/errores controlados; Backend usa fallback determinístico. |
| OCI deshabilitado | `ml-service` no descarga artefactos y reporta estado `disabled` en health. |
| OCI no disponible en demo | No exponer secretos en logs; usar fallback o detener despliegue según decisión del equipo. |
| Formato de artefacto incompatible | Mantener contrato HTTP estable y coordinar corrección con DS. |
| Salida ML incompleta o inválida | Backend no debe romper el wrapper público; valida/maneja la salida antes de responder. |

## Seguridad OCI

Para demo, seguir la guía de [`oci-object-storage-artefactos.md`](oci-object-storage-artefactos.md):

- Bucket privado.
- Sin credenciales en Git.
- Sin URLs firmadas en issues, PRs o logs.
- Evidencia segura sin identificadores sensibles.
- Archivos sin datos financieros reales de usuarios.

## Checklist para cerrar la Issue #19

- [x] Estrategia local implementada: `ml-service` carga archivos locales no versionados.
- [x] Estrategia demo implementada: descarga opcional desde OCI Object Storage privado.
- [x] Nombres reales de artefactos definidos.
- [x] Variables de configuración documentadas.
- [x] Contrato alineado con el mapeo Backend ↔ DS de la Issue #29.
- [x] Data Science confirma extensión/formato final de los artefactos integrados.
- [x] Backend implementa adaptadores HTTP hacia `ml-service`.
- [x] Fallback determinístico permanece activo ante errores de ML.
- [ ] Owner OCI confirma bucket y upload de prueba con evidencia segura cuando aplique a demo.

## Fuera de alcance

- Entrenar modelos nuevos.
- Implementar persistencia.
- Guardar artefactos o modelos en Git.
- Agregar credenciales OCI al repositorio.
- Cambiar el contrato público de `POST /analisis-financiero`.
