from __future__ import annotations

from collections.abc import Mapping, Sequence
from datetime import date, datetime
from decimal import Decimal
from typing import Any


DEFAULT_FEATURE_COLUMNS = [
    "ingresos_mensuales_usd",
    "egresos_total_mensual_usd",
    "nivel_endeudamiento",
    "gasto_ingreso_ratio",
    "frecuencia_ahorro_Alta",
    "frecuencia_ahorro_Baja",
    "frecuencia_ahorro_Media",
    "frecuencia_ahorro_Nula",
]

SAVING_FREQUENCY_COLUMNS = {
    "ALTA": "frecuencia_ahorro_Alta",
    "BAJA": "frecuencia_ahorro_Baja",
    "MEDIA": "frecuencia_ahorro_Media",
    "NULA": "frecuencia_ahorro_Nula",
}


def build_feature_row(payload: Mapping[str, Any]) -> dict[str, Any]:
    transactions = payload.get("transacciones") or []
    expense_total = 0.0

    for transaction in transactions:
        amount = _to_float(transaction.get("monto"))
        transaction_type = str(transaction.get("tipo") or "").lower()

        if transaction_type == "egreso":
            expense_total += amount

    ingreso_mensual = _to_float(payload.get("ingreso_mensual"))
    pago_mensual_deudas = _to_float(payload.get("pago_mensual_deudas"))
    saving_frequency = str(payload.get("frecuencia_ahorro") or "").strip().upper()

    row = {
        "ingresos_mensuales_usd": ingreso_mensual,
        "egresos_total_mensual_usd": expense_total,
        # Backend docs mention nivel_endeudamiento; with this payload, monthly debt payment
        # over monthly income is the available debt pressure signal. The model expects percent.
        "nivel_endeudamiento": _safe_ratio(pago_mensual_deudas, ingreso_mensual) * 100,
        "gasto_ingreso_ratio": _safe_ratio(expense_total, ingreso_mensual),
        "frecuencia_ahorro_Alta": 0,
        "frecuencia_ahorro_Baja": 0,
        "frecuencia_ahorro_Media": 0,
        "frecuencia_ahorro_Nula": 0,
    }

    frequency_column = SAVING_FREQUENCY_COLUMNS.get(saving_frequency)
    if frequency_column is not None:
        row[frequency_column] = 1

    return row


def build_model_input(payload: Mapping[str, Any], model: Any | None = None) -> Any:
    row = build_feature_row(payload)
    feature_columns = _model_feature_names(model) or DEFAULT_FEATURE_COLUMNS
    missing_features = [column for column in feature_columns if column not in row]

    if missing_features:
        missing = ", ".join(missing_features)
        raise ValueError(f"Cannot build model input; missing expected feature(s): {missing}")

    ordered_row = {column: row[column] for column in feature_columns}

    try:
        import pandas as pd
    except ImportError:
        return [ordered_row]

    return pd.DataFrame([ordered_row], columns=feature_columns)


def run_inference(model: Any, payload: Mapping[str, Any]) -> dict[str, Any]:
    if not hasattr(model, "predict"):
        raise TypeError("Loaded model does not expose a predict() method")

    model_input = build_model_input(payload, model)
    prediction_output = model.predict(model_input)
    prediction = _first_value(prediction_output)

    response: dict[str, Any] = {"prediction": to_jsonable(prediction)}

    if hasattr(model, "predict_proba"):
        probability_output = model.predict_proba(model_input)
        confidence = _max_probability(_first_value(probability_output))
        if confidence is not None:
            response["confidence"] = confidence

    return response


def model_metadata(model: Any, model_path: str) -> dict[str, Any]:
    metadata = {
        "model_path": model_path,
        "model_type": type(model).__name__,
        "model_module": type(model).__module__,
        "supports_predict_proba": hasattr(model, "predict_proba"),
    }

    n_features = getattr(model, "n_features_in_", None)
    if n_features is not None:
        metadata["n_features_in"] = to_jsonable(n_features)

    feature_names = _model_feature_names(model)
    if feature_names is not None:
        metadata["feature_names_in"] = feature_names

    return metadata


def to_jsonable(value: Any) -> Any:
    if isinstance(value, (str, int, float, bool)) or value is None:
        return value
    if isinstance(value, Decimal):
        return float(value)
    if isinstance(value, (datetime, date)):
        return value.isoformat()
    if isinstance(value, Mapping):
        return {str(key): to_jsonable(item) for key, item in value.items()}
    if isinstance(value, Sequence) and not isinstance(value, (str, bytes, bytearray)):
        return [to_jsonable(item) for item in value]

    item = getattr(value, "item", None)
    if callable(item):
        try:
            return to_jsonable(item())
        except (TypeError, ValueError):
            pass

    tolist = getattr(value, "tolist", None)
    if callable(tolist):
        return to_jsonable(tolist())

    return str(value)


def _first_value(value: Any) -> Any:
    jsonable = to_jsonable(value)
    if isinstance(jsonable, list) and jsonable:
        return jsonable[0]
    return jsonable


def _max_probability(value: Any) -> float | None:
    jsonable = to_jsonable(value)
    if isinstance(jsonable, list) and jsonable:
        numbers = [item for item in jsonable if isinstance(item, (int, float))]
        if numbers:
            return float(max(numbers))
    if isinstance(jsonable, (int, float)):
        return float(jsonable)
    return None


def _safe_ratio(numerator: float, denominator: float) -> float:
    if denominator == 0:
        return 0.0
    return numerator / denominator


def _model_feature_names(model: Any | None) -> list[str] | None:
    feature_names = getattr(model, "feature_names_in_", None)
    if feature_names is None:
        return None
    return [str(feature_name) for feature_name in to_jsonable(feature_names)]


def _to_float(value: Any) -> float:
    if value is None:
        return 0.0
    return float(value)
