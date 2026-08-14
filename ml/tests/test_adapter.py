from decimal import Decimal

import pytest

from app.adapter import DEFAULT_FEATURE_COLUMNS, build_feature_row, build_model_input, run_inference, to_jsonable


SAMPLE_PAYLOAD = {
    "credito_total": Decimal("15000"),
    "ingreso_mensual": Decimal("4500"),
    "frecuencia_ahorro": "MEDIA",
    "pago_mensual_deudas": Decimal("600"),
    "transacciones": [
        {
            "tipo": "Ingreso",
            "fecha": "2026-07-01",
            "descripcion": "Salary",
            "monto": Decimal("4500"),
        },
        {
            "tipo": "Egreso",
            "fecha": "2026-07-05",
            "descripcion": "Groceries",
            "tipo_pago": "Debito",
            "monto": Decimal("350"),
        },
        {
            "tipo": "Egreso",
            "fecha": "2026-07-10",
            "descripcion": "Computer",
            "tipo_pago": "Credito",
            "meses_a_deber": 12,
            "monto": Decimal("1200"),
        },
    ],
}


class DummyModel:
    feature_names_in_ = DEFAULT_FEATURE_COLUMNS

    def predict(self, model_input):
        assert model_input is not None
        return ["stable"]

    def predict_proba(self, model_input):
        assert model_input is not None
        return [[0.2, 0.8]]


class RegressorModel:
    feature_names_in_ = DEFAULT_FEATURE_COLUMNS

    def predict(self, model_input):
        assert list(model_input.columns) == DEFAULT_FEATURE_COLUMNS
        return [72.5]


def test_build_feature_row_uses_real_model_features_in_order():
    row = build_feature_row(SAMPLE_PAYLOAD)

    assert list(row) == DEFAULT_FEATURE_COLUMNS
    assert row["ingresos_mensuales_usd"] == 4500.0
    assert row["egresos_total_mensual_usd"] == 1550.0
    assert row["nivel_endeudamiento"] == (600 / 4500) * 100
    assert row["gasto_ingreso_ratio"] == 1550 / 4500


def test_build_feature_row_one_hot_encodes_saving_frequency():
    row = build_feature_row(SAMPLE_PAYLOAD | {"frecuencia_ahorro": "ALTA"})

    assert row["frecuencia_ahorro_Alta"] == 1
    assert row["frecuencia_ahorro_Baja"] == 0
    assert row["frecuencia_ahorro_Media"] == 0
    assert row["frecuencia_ahorro_Nula"] == 0


def test_build_model_input_aligns_to_model_feature_names():
    class ReorderedModel:
        feature_names_in_ = [
            "frecuencia_ahorro_Media",
            "gasto_ingreso_ratio",
            "ingresos_mensuales_usd",
        ]

    model_input = build_model_input(SAMPLE_PAYLOAD, ReorderedModel())

    assert list(model_input.columns) == ReorderedModel.feature_names_in_
    assert model_input.iloc[0].to_dict() == {
        "frecuencia_ahorro_Media": 1,
        "gasto_ingreso_ratio": 1550 / 4500,
        "ingresos_mensuales_usd": 4500.0,
    }


def test_build_model_input_fails_when_model_feature_cannot_be_built():
    class UnsupportedFeatureModel:
        feature_names_in_ = ["unsupported_feature"]

    with pytest.raises(ValueError, match="unsupported_feature"):
        build_model_input(SAMPLE_PAYLOAD, UnsupportedFeatureModel())


def test_run_inference_serializes_prediction_and_confidence():
    result = run_inference(DummyModel(), SAMPLE_PAYLOAD)

    assert result == {"prediction": "stable", "confidence": 0.8}


def test_run_inference_allows_regressor_without_confidence():
    result = run_inference(RegressorModel(), SAMPLE_PAYLOAD)

    assert result == {"prediction": 72.5}


def test_to_jsonable_converts_numpy_like_scalars():
    class NumpyLikeScalar:
        def item(self):
            return Decimal("7.5")

    assert to_jsonable(NumpyLikeScalar()) == 7.5
