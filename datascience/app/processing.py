import numpy as np
import pandas as pd

# --- Categoría ---
TIPO_TRANSACCION_MAP = {"Egreso": 0, "Ingreso": 1}
FEATURE_ORDER_CATEGORIA = ["dia", "mes", "anio", "valor_usd", "tipo_transaccion"]

# --- Financiero ---
FRECUENCIA_AHORRO_CATEGORIAS = ["Alta", "Baja", "Media", "Nula"] 
FRECUENCIA_AHORRO_MAP = { "Nula": 0, "Baja": 1, "Media": 2, "Alta": 3 }
FEATURE_ORDER_FINANCIERO = [
    "ingresos_mensuales_usd",
    "egresos_total_mensual_usd",
    "nivel_endeudamiento",
    "gasto_ingreso_ratio",
    "frecuencia_ahorro_Alta",
    "frecuencia_ahorro_Baja",
    "frecuencia_ahorro_Media",
    "frecuencia_ahorro_Nula",
]

# ---------- Modelo de categoría ----------

def build_categoria_features(transaccion, scaler) -> np.ndarray:
    monto_log = np.log1p(transaccion.monto)
    dia, mes, anio = transaccion.fecha.split("-")
    dia, mes, anio = int(dia), int(mes), int(anio)
    tipo_transaccion = TIPO_TRANSACCION_MAP.get(transaccion.tipo, 0)
    

    X = pd.DataFrame([{
        "dia": dia,
        "mes": mes,
        "anio": anio,
        "valor_usd": monto_log,
        "tipo_transaccion": tipo_transaccion
    }])

    X = X[FEATURE_ORDER_CATEGORIA]
    return X

def predict_categoria(transaccion, models, scalers, label_encoders) -> str:
    scaler =scalers
    label_encoder = label_encoders
    rf_model = models

    X = build_categoria_features(transaccion, scaler)
    pred_encoded = rf_model.predict(X)
    categoria = label_encoder.inverse_transform(pred_encoded)[0]
    return str(categoria).lower()

# ---------- Indicadores financieros ----------

def compute_indicadores(payload, clasificadas):
    gastos_por_categoria: dict[str, float] = {}
    gasto_total = 0.0

    for t in clasificadas:
        if t.tipo == "Egreso":
            gasto_total += t.monto
            cat = t.categoria or "otros"
            gastos_por_categoria[cat] = gastos_por_categoria.get(cat, 0.0) + t.monto

    deuda_total = payload.credito_total * (payload.nivel_endeudamiento / 100)

    ratio_pago_deudas = (
        round(payload.pago_mensual_deudas / payload.ingreso_mensual, 4)
        if payload.ingreso_mensual else 0.0
    )
    ratio_deuda_ingreso = (
        round(deuda_total / payload.ingreso_mensual, 4)
        if payload.ingreso_mensual else 0.0
    )

    def pct(cat: str) -> float:
        return round((gastos_por_categoria.get(cat, 0.0) / gasto_total) * 100, 2) if gasto_total else 0.0

    indicadores = {
        "ingreso_mensual": payload.ingreso_mensual,
        "deuda_total": deuda_total,
        "credito_total": payload.credito_total,
        "frecuencia_ahorro": payload.frecuencia_ahorro,
        "nivel_endeudamiento": payload.nivel_endeudamiento,
        "pago_mensual_deudas": payload.pago_mensual_deudas,
        "gasto_total": gasto_total,
        "ratio_pago_deudas": ratio_pago_deudas,
        "ratio_deuda_ingreso": ratio_deuda_ingreso,
        "porcentaje_alimentos": pct("alimentacion"),
        "porcentaje_transporte": pct("transporte"),
        "porcentaje_entretenimiento": pct("entretenimiento"),
        "porcentaje_salud": pct("salud"),
        "porcentaje_vivienda": pct("vivienda"),
        "porcentaje_educacion": pct("educacion"),
        "porcentaje_viajes": pct("viaje"),
        "porcentaje_servicios": pct("servicios"),
        "porcentaje_otros": pct("otro") + pct("compras") + pct("facturas"),
    }
    return indicadores, gastos_por_categoria

# ---------- Modelos de estabilidad y perfil ----------

def build_stability_features(payload, indicadores: dict) -> np.ndarray:
    gasto_ingreso_ratio = (
        indicadores["gasto_total"] / payload.ingreso_mensual if payload.ingreso_mensual else 0.0
    )
    frecuencia_onehot = {
        f"frecuencia_ahorro_{c}": (1 if payload.frecuencia_ahorro == c else 0)
        for c in FRECUENCIA_AHORRO_CATEGORIAS
    }

    values = {
        "ingresos_mensuales_usd": payload.ingreso_mensual,
        "egresos_total_mensual_usd": indicadores["gasto_total"],
        "nivel_endeudamiento": payload.nivel_endeudamiento,
        "gasto_ingreso_ratio": gasto_ingreso_ratio,
        **frecuencia_onehot,
    }
    return np.array([[values[f] for f in FEATURE_ORDER_FINANCIERO]])

def build_perfil_features( payload, indicadores ) -> np.ndarray:

    frecuencia = FRECUENCIA_AHORRO_MAP.get(
        payload.frecuencia_ahorro,
        0
    )

    X = np.array([[
        payload.ingreso_mensual,
        indicadores["gasto_total"],
        frecuencia
    ]], dtype=float)

    return X

def predict_score_financiero(payload,indicadores: dict,model) -> float:

    X = build_stability_features(payload,indicadores)
    score = model.predict(X)[0]
    return round(float(score), 2)


def predict_perfil_financiero( payload, indicadores: dict, model) -> str:

    X = build_perfil_features( payload, indicadores )
    pred = model.predict(X)[0]
    return str(pred)



