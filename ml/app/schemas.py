from __future__ import annotations

from datetime import date
from decimal import Decimal
from typing import Any, Literal

from pydantic import BaseModel, ConfigDict, Field


class Transaction(BaseModel):
    tipo: Literal["Ingreso", "Egreso"]
    fecha: date
    descripcion: str = Field(min_length=1, max_length=200)
    tipo_pago: Literal["Efectivo", "Debito", "Credito"] | None = None
    meses_a_deber: int | None = Field(default=None, gt=0)
    monto: Decimal = Field(gt=0)


class FinancialAnalysisRequest(BaseModel):
    credito_total: Decimal = Field(ge=0)
    ingreso_mensual: Decimal = Field(gt=0)
    frecuencia_ahorro: Literal["NULA", "BAJA", "MEDIA", "ALTA"]
    pago_mensual_deudas: Decimal = Field(ge=0)
    transacciones: list[Transaction] = Field(min_length=1, max_length=100)


class HealthResponse(BaseModel):
    status: Literal["ok", "degraded"]
    model_loaded: bool
    model_path: str
    model_error: str | None = None
    classifier_loaded: bool = False
    classifier_path: str | None = None
    label_encoder_loaded: bool = False
    label_encoder_path: str | None = None
    scaler_loaded: bool = False
    scaler_path: str | None = None
    classifier_error: str | None = None
    artifact_state: dict[str, Any] | None = None
    metadata: dict[str, Any] | None = None


class PredictionResponse(BaseModel):
    prediction: Any
    confidence: float | None = None
    metadata: dict[str, Any]

    model_config = ConfigDict(arbitrary_types_allowed=True)


class ClassifyTransactionsRequest(BaseModel):
    transacciones: list[Transaction] = Field(min_length=1, max_length=100)


class ClassifiedTransaction(Transaction):
    categoria: str


class ClassifyTransactionsResponse(BaseModel):
    transacciones: list[ClassifiedTransaction]
