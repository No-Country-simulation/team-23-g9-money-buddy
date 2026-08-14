from datetime import date
from decimal import Decimal

import pytest

from app.classifier import (
    CLASSIFIER_FEATURE_COLUMNS,
    build_classifier_feature_row,
    classify_transaction_category,
    deterministic_transaction_category,
    normalize_category,
)


class FakeScaler:
    def __init__(self):
        self.calls = []

    def transform(self, features):
        self.calls.append(("transform", list(features.columns)))
        return [[10, 20, 30, 40, 50]]


class FakeClassifier:
    def __init__(self, scaler):
        self.scaler = scaler
        self.calls = []

    def predict(self, features):
        assert self.scaler.calls == [("transform", CLASSIFIER_FEATURE_COLUMNS)]
        assert list(features.columns) == CLASSIFIER_FEATURE_COLUMNS
        assert features.iloc[0].to_list() == [10, 20, 30, 40, 50]
        self.calls.append(("predict", list(features.columns)))
        return [12]


class FakeLabelEncoder:
    def inverse_transform(self, values):
        assert values == [12]
        return ["Vivienda"]


def test_build_classifier_feature_row_uses_iso_day_month_year():
    row = build_classifier_feature_row(
        {
            "tipo": "Egreso",
            "fecha": date.fromisoformat("2026-07-05"),
            "monto": Decimal("350"),
        }
    )

    assert row["dia"] == 5
    assert row["mes"] == 7
    assert row["anio"] == 2026
    assert row["tipo_transaccion"] == 0


def test_classify_transaction_category_scales_features_before_prediction():
    scaler = FakeScaler()
    classifier = FakeClassifier(scaler)

    category = classify_transaction_category(
        classifier,
        FakeLabelEncoder(),
        scaler,
        {
            "tipo": "Egreso",
            "fecha": "2026-07-05",
            "monto": Decimal("350"),
        },
    )

    assert category == "vivienda"
    assert classifier.calls == [("predict", CLASSIFIER_FEATURE_COLUMNS)]


@pytest.mark.parametrize(
    ("description", "expected"),
    [
        ("Supermercado Walmart", "alimentos"),
        ("Taxi al trabajo", "transporte"),
        ("Farmacia medicina", "salud"),
        ("Pago alquiler departamento", "vivienda"),
        ("Curso online programación", "educacion"),
        ("Netflix y cine", "ocio_entretenimiento"),
        ("Factura internet y teléfono", "servicios"),
        ("Zapatillas deportivas", "ropa_calzado"),
        ("Laptop nueva", "tecnologia"),
        ("Consulta médica", "salud"),
    ],
)
def test_deterministic_transaction_category_classifies_clear_descriptions(description, expected):
    assert deterministic_transaction_category({"tipo": "Egreso", "descripcion": description}) == expected


def test_classify_transaction_category_uses_deterministic_category_before_model():
    scaler = FakeScaler()
    classifier = FakeClassifier(scaler)

    category = classify_transaction_category(
        classifier,
        FakeLabelEncoder(),
        scaler,
        {
            "tipo": "Egreso",
            "fecha": "2026-07-02",
            "descripcion": "Supermercado Walmart",
            "monto": Decimal("185.75"),
        },
    )

    assert category == "alimentos"
    assert scaler.calls == []
    assert classifier.calls == []


@pytest.mark.parametrize(
    ("raw_category", "expected"),
    [
        ("alimentacion", "alimentos"),
        ("entretenimiento", "ocio_entretenimiento"),
        ("otro", "otros"),
        ("viaje", "otros"),
        ("compras", "otros"),
        ("facturas", "otros"),
    ],
)
def test_normalize_category_maps_ds_labels_to_official_keys(raw_category, expected):
    assert normalize_category(raw_category) == expected
