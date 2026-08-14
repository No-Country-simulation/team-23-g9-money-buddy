from __future__ import annotations

import os
import pickle
from contextlib import asynccontextmanager
from pathlib import Path
from typing import Any

from fastapi import FastAPI, HTTPException

from app.adapter import model_metadata, run_inference
from app.classifier import (
    DEFAULT_CLASSIFIER_PATH,
    DEFAULT_LABEL_ENCODER_PATH,
    DEFAULT_SCALER_CATEGORIA_PATH,
    classify_transaction_category,
    load_pickle_artifact,
)
from app.oci_artifacts import download_artifacts_if_enabled
from app.schemas import (
    ClassifiedTransaction,
    ClassifyTransactionsRequest,
    ClassifyTransactionsResponse,
    FinancialAnalysisRequest,
    HealthResponse,
    PredictionResponse,
)


DEFAULT_MODEL_PATH = Path(__file__).resolve().parents[1] / "models" / "modelo_financial_stability.pkl"


class ModelState:
    model: Any | None = None
    error: str | None = None
    path: str = str(DEFAULT_MODEL_PATH)
    classifier: Any | None = None
    classifier_error: str | None = None
    classifier_path: str = str(DEFAULT_CLASSIFIER_PATH)
    label_encoder: Any | None = None
    label_encoder_path: str = str(DEFAULT_LABEL_ENCODER_PATH)
    scaler: Any | None = None
    scaler_path: str = str(DEFAULT_SCALER_CATEGORIA_PATH)
    artifact_state: dict[str, Any] | None = None


state = ModelState()


def load_model(model_path: str) -> Any:
    try:
        import joblib
    except ImportError:
        joblib = None

    if joblib is not None:
        return joblib.load(model_path)


    with Path(model_path).open("rb") as model_file:
        return pickle.load(model_file)


@asynccontextmanager
async def lifespan(app: FastAPI):
    state.path = os.getenv("MODEL_PATH", str(DEFAULT_MODEL_PATH))
    state.classifier_path = os.getenv("CLASSIFIER_MODEL_PATH", str(DEFAULT_CLASSIFIER_PATH))
    state.label_encoder_path = os.getenv("LABEL_ENCODER_CATEGORIA_PATH", str(DEFAULT_LABEL_ENCODER_PATH))
    state.scaler_path = os.getenv("SCALER_CATEGORIA_PATH", str(DEFAULT_SCALER_CATEGORIA_PATH))

    try:
        state.artifact_state = download_artifacts_if_enabled()
    except Exception as exc:  # noqa: BLE001 - health exposes a controlled failure state.
        state.artifact_state = {"enabled": True, "error": f"Artifact download failed: {exc}"}

    try:
        state.model = load_model(state.path)
        state.error = None
    except Exception as exc:  # noqa: BLE001 - health and predict expose a controlled failure state.
        state.model = None
        state.error = f"Model failed to load: {exc}"

    try:
        classifier_path = Path(state.classifier_path)
        label_encoder_path = Path(state.label_encoder_path)
        scaler_path = Path(state.scaler_path)
        state.classifier = load_pickle_artifact(str(classifier_path)) if classifier_path.exists() else None
        state.label_encoder = load_pickle_artifact(str(label_encoder_path)) if label_encoder_path.exists() else None
        state.scaler = load_pickle_artifact(str(scaler_path)) if scaler_path.exists() else None
        missing = []
        if state.classifier is None:
            missing.append("classifier model")
        if state.label_encoder is None:
            missing.append("label encoder")
        if state.scaler is None:
            missing.append("category scaler")
        state.classifier_error = f"Missing {' and '.join(missing)}" if missing else None
    except Exception as exc:  # noqa: BLE001 - endpoint exposes a controlled failure state.
        state.classifier = None
        state.label_encoder = None
        state.scaler = None
        state.classifier_error = f"Classifier artifacts failed to load: {exc}"

    yield


app = FastAPI(
    title="MoneyBuddy ML Service",
    description="Serves the financial stability model for backend integration.",
    version="0.1.0",
    lifespan=lifespan,
)


@app.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    metadata = model_metadata(state.model, state.path) if state.model is not None else None
    return HealthResponse(
        status="ok" if state.model is not None else "degraded",
        model_loaded=state.model is not None,
        model_path=state.path,
        model_error=state.error,
        classifier_loaded=state.classifier is not None,
        classifier_path=state.classifier_path,
        label_encoder_loaded=state.label_encoder is not None,
        label_encoder_path=state.label_encoder_path,
        scaler_loaded=state.scaler is not None,
        scaler_path=state.scaler_path,
        classifier_error=state.classifier_error,
        artifact_state=state.artifact_state,
        metadata=metadata,
    )


@app.post("/predict", response_model=PredictionResponse)
def predict(request: FinancialAnalysisRequest) -> PredictionResponse:
    if state.model is None:
        raise HTTPException(status_code=503, detail=state.error or "Model is not loaded")

    payload = request.model_dump(mode="python")

    try:
        result = run_inference(state.model, payload)
    except Exception as exc:  # noqa: BLE001 - API clients need a stable HTTP error, not raw tracebacks.
        raise HTTPException(status_code=500, detail=f"Inference failed: {exc}") from exc

    return PredictionResponse(
        prediction=result["prediction"],
        confidence=result.get("confidence"),
        metadata=model_metadata(state.model, state.path),
    )


@app.post("/classify-transactions", response_model=ClassifyTransactionsResponse)
def classify_transactions(request: ClassifyTransactionsRequest) -> ClassifyTransactionsResponse:
    has_expense = any(transaction.tipo == "Egreso" for transaction in request.transacciones)
    if has_expense and (state.classifier is None or state.label_encoder is None or state.scaler is None):
        raise HTTPException(
            status_code=503,
            detail=state.classifier_error or "Transaction classifier, label encoder, and category scaler are required",
        )

    classified_transactions = []
    for transaction in request.transacciones:
        payload = transaction.model_dump(mode="python")
        if transaction.tipo == "Ingreso":
            category = "ingreso"
        else:
            try:
                category = classify_transaction_category(state.classifier, state.label_encoder, state.scaler, payload)
            except Exception as exc:  # noqa: BLE001 - API clients need a stable HTTP error, not raw tracebacks.
                raise HTTPException(status_code=500, detail=f"Transaction classification failed: {exc}") from exc

        classified_transactions.append(ClassifiedTransaction(**payload, categoria=category))

    return ClassifyTransactionsResponse(transacciones=classified_transactions)
