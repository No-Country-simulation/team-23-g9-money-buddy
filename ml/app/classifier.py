from __future__ import annotations

import math
import pickle
import unicodedata
from collections.abc import Mapping
from datetime import date, datetime
from decimal import Decimal
from pathlib import Path
from typing import Any

from app.adapter import to_jsonable


DEFAULT_CLASSIFIER_PATH = Path(__file__).resolve().parents[1] / "models" / "classify-transactions.pkl"
DEFAULT_LABEL_ENCODER_PATH = Path(__file__).resolve().parents[1] / "models" / "label_encoder_categoria.pkl"
DEFAULT_SCALER_CATEGORIA_PATH = Path(__file__).resolve().parents[1] / "models" / "scaler_categoria.pkl"
CLASSIFIER_FEATURE_COLUMNS = ["dia", "mes", "anio", "valor_usd", "tipo_transaccion"]

CATEGORY_NORMALIZATION = {
    "alimentacion": "alimentos",
    "alimentos": "alimentos",
    "entretenimiento": "ocio_entretenimiento",
    "ocio_entretenimiento": "ocio_entretenimiento",
    "otro": "otros",
    "otros": "otros",
    "viaje": "otros",
    "viajes": "otros",
    "compras": "otros",
    "facturas": "otros",
}


def load_pickle_artifact(path: str) -> Any:
    try:
        import joblib
    except ImportError:
        joblib = None

    if joblib is not None:
        return joblib.load(path)

    with Path(path).open("rb") as artifact_file:
        return pickle.load(artifact_file)


def build_classifier_feature_row(transaction: Mapping[str, Any]) -> dict[str, Any]:
    transaction_date = _to_date(transaction.get("fecha"))
    transaction_type = str(transaction.get("tipo") or "").strip().lower()
    return {
        "dia": transaction_date.day,
        "mes": transaction_date.month,
        "anio": transaction_date.year,
        "valor_usd": math.log1p(_to_float(transaction.get("monto"))),
        "tipo_transaccion": 1 if transaction_type == "ingreso" else 0,
    }


def build_classifier_input(transaction: Mapping[str, Any]) -> Any:
    row = build_classifier_feature_row(transaction)

    try:
        import pandas as pd
    except ImportError:
        return [[row[column] for column in CLASSIFIER_FEATURE_COLUMNS]]

    return pd.DataFrame([{column: row[column] for column in CLASSIFIER_FEATURE_COLUMNS}], columns=CLASSIFIER_FEATURE_COLUMNS)


def classify_transaction_category(classifier: Any, label_encoder: Any, scaler: Any, transaction: Mapping[str, Any]) -> str:
    deterministic_category = deterministic_transaction_category(transaction)
    if deterministic_category != "otros":
        return deterministic_category

    if not hasattr(scaler, "transform"):
        raise TypeError("Loaded category scaler does not expose transform()")

    model_input = build_classifier_input(transaction)
    scaled_input = scaler.transform(model_input)
    prediction_input = _with_classifier_columns(scaled_input, model_input)
    prediction = classifier.predict(prediction_input)
    numeric_category = _first_value(prediction)

    if not hasattr(label_encoder, "inverse_transform"):
        raise TypeError("Loaded label encoder does not expose inverse_transform()")

    label = _first_value(label_encoder.inverse_transform([numeric_category]))
    return normalize_category(str(label))


def deterministic_transaction_category(transaction: Mapping[str, Any]) -> str:
    if str(transaction.get("tipo") or "").strip().lower() == "ingreso":
        return "ingreso"

    description = _normalize_text(str(transaction.get("descripcion") or ""))

    if _contains_any(description, "supermercado", "mercado", "comida", "restaurante", "cafe", "delivery", "alimento", "cena"):
        return "alimentos"
    if _contains_any(description, "bus", "taxi", "uber", "transporte", "metro", "combustible", "gasolina", "auto"):
        return "transporte"
    if _contains_any(description, "farmacia", "medico", "medica", "medicina", "salud", "hospital", "clinica", "consulta"):
        return "salud"
    if _contains_any(description, "alquiler", "renta", "hipoteca", "departamento"):
        return "vivienda"
    if _contains_any(description, "colegio", "universidad", "curso", "libro", "educacion"):
        return "educacion"
    if _contains_any(description, "luz", "agua", "internet", "servicio", "telefono", "gas", "factura"):
        return "servicios"
    if _contains_any(description, "cine", "netflix", "streaming", "juego", "entretenimiento", "ocio"):
        return "ocio_entretenimiento"
    if _contains_any(description, "ropa", "zapato", "zapatilla", "calzado", "accesorio", "camisa", "pantalon"):
        return "ropa_calzado"
    if _contains_any(description, "laptop", "notebook", "computadora", "celular", "smartphone", "electronica", "tecnologia"):
        return "tecnologia"

    return "otros"


def _with_classifier_columns(scaled_input: Any, raw_input: Any) -> Any:
    if not hasattr(raw_input, "columns"):
        return scaled_input

    try:
        import pandas as pd
    except ImportError:
        return scaled_input

    return pd.DataFrame(scaled_input, columns=list(raw_input.columns), index=getattr(raw_input, "index", None))


def normalize_category(category: str) -> str:
    normalized = _normalize_text(category).replace(" ", "_").replace("-", "_")
    return CATEGORY_NORMALIZATION.get(normalized, normalized)


def _normalize_text(value: str) -> str:
    normalized = unicodedata.normalize("NFKD", value.strip().lower())
    return "".join(char for char in normalized if not unicodedata.combining(char))


def _contains_any(value: str, *candidates: str) -> bool:
    return any(candidate in value for candidate in candidates)


def _first_value(value: Any) -> Any:
    jsonable = to_jsonable(value)
    if isinstance(jsonable, list) and jsonable:
        return jsonable[0]
    return jsonable


def _to_date(value: Any) -> date:
    if isinstance(value, datetime):
        return value.date()
    if isinstance(value, date):
        return value
    if isinstance(value, str):
        return date.fromisoformat(value)
    raise ValueError("Transaction fecha must be an ISO date")


def _to_float(value: Any) -> float:
    if value is None:
        return 0.0
    if isinstance(value, Decimal):
        return float(value)
    return float(value)
