# MoneyBuddy ML Service

`ml-service` es el microservicio FastAPI de Money Buddy. Carga los artefactos ML al iniciar y expone inferencia para el backend Spring Boot sin cambiar el contrato público de `POST /analisis-financiero`.

## Ejecución local

```bash
cd ml
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

Si necesitás cambiar la ruta del modelo de estabilidad financiera:

```bash
MODEL_PATH=./models/modelo_financial_stability.pkl uvicorn app.main:app --host 0.0.0.0 --port 8000
```

Rutas configurables para el clasificador de transacciones:

```bash
CLASSIFIER_MODEL_PATH=./models/classify-transactions.pkl \
LABEL_ENCODER_CATEGORIA_PATH=./models/label_encoder_categoria.pkl \
SCALER_CATEGORIA_PATH=./models/scaler_categoria.pkl \
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

## Endpoints

| Endpoint | Uso |
| --- | --- |
| `GET /health` | Informa estado de carga del modelo financiero, clasificador, label encoder, scaler y descarga OCI. |
| `POST /predict` | Ejecuta el modelo de estabilidad financiera con el payload compatible con backend. |
| `POST /classify-transactions` | Devuelve las transacciones recibidas con `categoria`. |

## Artefactos de modelos

El servicio espera estos archivos bajo `ml/models/` o en las rutas indicadas por variables de entorno:

| Archivo | Uso |
| --- | --- |
| `modelo_financial_stability.pkl` | Modelo de estabilidad financiera usado por `/predict`. |
| `classify-transactions.pkl` | Clasificador RandomForest para categorías ambiguas. |
| `label_encoder_categoria.pkl` | Convierte la salida numérica del clasificador a categoría textual. |
| `scaler_categoria.pkl` | Se aplica antes de llamar al RandomForest de clasificación. |

## Descarga de artefactos OCI

La descarga desde OCI Object Storage está implementada, pero deshabilitada por defecto. Activala solo en entornos que ya tengan configuración y credenciales OCI disponibles.

Usá `ml/.env.example` como plantilla segura y completá valores reales fuera de Git. No commitees `.env`, OCIDs, fingerprints, namespaces reales, llaves privadas ni URLs firmadas.

```bash
OCI_MODEL_DOWNLOAD_ENABLED=true
OCI_MODEL_FORCE_DOWNLOAD=false
OCI_CONFIG_FILE=~/.oci/config
OCI_CONFIG_PROFILE=DEFAULT
OCI_NAMESPACE=<namespace>
OCI_BUCKET_NAME=<bucket-name>
```

Objetos configurables:

```bash
OCI_FINANCIAL_STABILITY_OBJECT="financial-modelsmodelo_financial_stability.pkl"
OCI_TRANSACTION_CLASSIFIER_OBJECT="financial-modelsrandom_forest_model (champion).pkl"
OCI_LABEL_ENCODER_CATEGORIA_OBJECT="financial-modelslabel_encoder_categoria.pkl"
OCI_SCALER_CATEGORIA_OBJECT="financial-modelsscaler_categoria.pkl"
```

## Docker Compose

```bash
docker compose up --build ml-service
```

El servicio queda disponible en `http://localhost:8000`.

## Health check

```bash
curl http://localhost:8000/health
```

La respuesta incluye `model_loaded`, `classifier_loaded`, `label_encoder_loaded`, `scaler_loaded`, rutas configuradas, errores controlados y estado de artefactos OCI.

## Predicción financiera

```bash
curl -X POST http://localhost:8000/predict \
  -H "Content-Type: application/json" \
  -d '{
    "credito_total": 15000,
    "ingreso_mensual": 4500,
    "frecuencia_ahorro": "MEDIA",
    "pago_mensual_deudas": 600,
    "transacciones": [
      {
        "tipo": "Ingreso",
        "fecha": "2026-07-01",
        "descripcion": "Salario mensual",
        "monto": 4500
      },
      {
        "tipo": "Egreso",
        "fecha": "2026-07-05",
        "descripcion": "Supermercado",
        "tipo_pago": "Debito",
        "monto": 350
      },
      {
        "tipo": "Egreso",
        "fecha": "2026-07-10",
        "descripcion": "Computadora",
        "tipo_pago": "Credito",
        "meses_a_deber": 12,
        "monto": 1200
      }
    ]
  }'
```

### Forma de respuesta

Las predicciones exitosas incluyen salida del modelo y metadata. El artefacto actual es un `RandomForestRegressor`; por eso `confidence` queda en `null` salvo que un modelo futuro exponga `predict_proba`.

```json
{
  "prediction": 72.5,
  "confidence": null,
  "metadata": {
    "model_path": "models/modelo_financial_stability.pkl",
    "model_type": "RandomForestRegressor",
    "model_module": "sklearn.ensemble._forest",
    "supports_predict_proba": false,
    "n_features_in": 8,
    "feature_names_in": [
      "ingresos_mensuales_usd",
      "egresos_total_mensual_usd",
      "nivel_endeudamiento",
      "gasto_ingreso_ratio",
      "frecuencia_ahorro_Alta",
      "frecuencia_ahorro_Baja",
      "frecuencia_ahorro_Media",
      "frecuencia_ahorro_Nula"
    ]
  }
}
```

El servicio arma esas columnas desde el payload compatible con backend y alinea el DataFrame con `feature_names_in_` cuando el modelo cargado lo expone.

Si el modelo no carga, `/predict` responde `503`. Si falla la inferencia, responde `500` con un mensaje controlado.

## Clasificación de transacciones

`/classify-transactions` acepta transacciones compatibles con backend y devuelve las mismas transacciones con `categoria`.

```bash
curl -X POST http://localhost:8000/classify-transactions \
  -H "Content-Type: application/json" \
  -d '{
    "transacciones": [
      {
        "tipo": "Ingreso",
        "fecha": "2026-07-01",
        "descripcion": "Salario mensual",
        "monto": 4500
      },
      {
        "tipo": "Egreso",
        "fecha": "2026-07-05",
        "descripcion": "Supermercado",
        "tipo_pago": "Debito",
        "monto": 350
      }
    ]
  }'
```

### Estrategia híbrida

La resolución de categoría usa esta prioridad:

1. Si `tipo` es `Ingreso`, devuelve `categoria: "ingreso"` sin usar ML.
2. Para `Egreso`, primero aplica reglas determinísticas sobre `descripcion`.
3. Si las reglas devuelven una categoría distinta de `otros`, esa categoría se usa como resultado final.
4. Si las reglas devuelven `otros`, recién ahí usa ML con `classify-transactions.pkl`, `label_encoder_categoria.pkl` y `scaler_categoria.pkl`.

El modelo clasificador no usa `descripcion` como feature. Sus entradas son `dia`, `mes`, `anio`, `valor_usd` y `tipo_transaccion`; `valor_usd` se calcula con `log1p(monto)`. La descripción solo participa en las reglas determinísticas previas.

Antes de predecir, el servicio aplica `scaler_categoria.pkl` a las features y luego llama al RandomForest. Si falta el clasificador, el label encoder o el scaler para clasificar egresos, el endpoint responde `503` en vez de inventar categorías.
