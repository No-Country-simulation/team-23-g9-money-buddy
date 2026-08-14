# MoneyBuddy ML Service

FastAPI microservice that loads `models/modelo_financial_stability.pkl` once at startup and exposes model inference for the backend.

## Run Locally

```bash
cd ml
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

Override the model path if needed:

```bash
MODEL_PATH=./models/modelo_financial_stability.pkl uvicorn app.main:app --host 0.0.0.0 --port 8000
```

Optional classifier paths:

```bash
CLASSIFIER_MODEL_PATH=./models/classify-transactions.pkl \
LABEL_ENCODER_CATEGORIA_PATH=./models/label_encoder_categoria.pkl \
SCALER_CATEGORIA_PATH=./models/scaler_categoria.pkl \
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

## OCI Model Artifacts

OCI Object Storage download is disabled by default. Enable it only in environments that already have OCI config and credentials available.

```bash
OCI_MODEL_DOWNLOAD_ENABLED=true
OCI_MODEL_FORCE_DOWNLOAD=false
OCI_CONFIG_FILE=~/.oci/config
OCI_CONFIG_PROFILE=DEFAULT
OCI_NAMESPACE=<namespace>
OCI_BUCKET_NAME=<bucket-name>
```

Optional object-name overrides:

```bash
OCI_FINANCIAL_STABILITY_OBJECT="financial-modelsmodelo_financial_stability.pkl"
OCI_TRANSACTION_CLASSIFIER_OBJECT="financial-modelsrandom_forest_model (champion).pkl"
OCI_LABEL_ENCODER_CATEGORIA_OBJECT="financial-modelslabel_encoder_categoria.pkl"
OCI_SCALER_CATEGORIA_OBJECT="financial-modelsscaler_categoria.pkl"
```

Targets under `models/`:

```text
modelo_financial_stability.pkl
classify-transactions.pkl
label_encoder_categoria.pkl
scaler_categoria.pkl
```

No secrets, OCIDs, fingerprints, private keys, or real namespace values should be committed.

## Docker Compose

```bash
docker compose up --build ml-service
```

The service is exposed at `http://localhost:8000`.

## Health Check

```bash
curl http://localhost:8000/health
```

The health response includes `classifier_loaded`, `label_encoder_loaded`, and `scaler_loaded` with their configured paths so `/classify-transactions` readiness is visible.

## Predict

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
        "descripcion": "Monthly salary",
        "monto": 4500
      },
      {
        "tipo": "Egreso",
        "fecha": "2026-07-05",
        "descripcion": "Groceries",
        "tipo_pago": "Debito",
        "monto": 350
      },
      {
        "tipo": "Egreso",
        "fecha": "2026-07-10",
        "descripcion": "Computer",
        "tipo_pago": "Credito",
        "meses_a_deber": 12,
        "monto": 1200
      }
    ]
  }'
```

## Response Shape

Successful predictions include the model output and metadata. The current artifact is a `RandomForestRegressor`, so `confidence` is absent unless a future model exposes `predict_proba`.

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

The service builds those feature columns from the backend-compatible request payload and aligns the request DataFrame to `feature_names_in_` when the loaded model exposes it.

If the model cannot be loaded, `/predict` returns `503`. If inference fails, `/predict` returns `500` with a clear error message.

## Classify Transactions

`/classify-transactions` accepts backend-compatible transactions and returns the same transactions with `categoria`.

```bash
curl -X POST http://localhost:8000/classify-transactions \
  -H "Content-Type: application/json" \
  -d '{
    "transacciones": [
      {
        "tipo": "Ingreso",
        "fecha": "2026-07-01",
        "descripcion": "Monthly salary",
        "monto": 4500
      },
      {
        "tipo": "Egreso",
        "fecha": "2026-07-05",
        "descripcion": "Groceries",
        "tipo_pago": "Debito",
        "monto": 350
      }
    ]
  }'
```

`Ingreso` transactions return `categoria: "ingreso"` without ML. `Egreso` transactions require `classify-transactions.pkl`, `label_encoder_categoria.pkl`, and `scaler_categoria.pkl`; otherwise the endpoint returns `503` instead of inventing categories. The service applies the category scaler before calling the RandomForest classifier.
