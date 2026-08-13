import logging
from fastapi import FastAPI, HTTPException
from contextlib import asynccontextmanager

from .schemas import (
    CategoriaData,
    CategoriaRequest,
    CategoriaResponse,
    PerfilFinancieroData,
    PerfilFinancieroRequest,
    PerfilFinancieroResponse,
    TransaccionClasificada,
)
from .processing import predict_categoria, compute_indicadores, predict_score_financiero, predict_perfil_financiero
from .models import load_models, get_model
from .recomendations import features, perfil_desde_features, evaluar_perfil_final

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("main")

# se activa al iniciar la aplicacion
@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Descargando y cargando modelos desde OCI Object Storage...")
    load_models()
    logger.info("Modelos listos, el servicio puede recibir tráfico.")
    yield
    logger.info("Apagando servicio.")

app = FastAPI( title="MoneyBuddy AI", version="1.0.0", lifespan=lifespan )

# Comando para ver el estado de la API
@app.get("/health")
def health():
    return {"status": "ok"}

# EndPoint para predecir la categoría de las transacciones
@app.post("/predecir-categoria", response_model=CategoriaResponse)
def predecir_categoria(request: CategoriaRequest) -> CategoriaResponse:
    try:
        model = get_model("random_forest_model")
        scaler = get_model("scaler_categoria")
        label_encoder = get_model("label_encoder")
        clasificadas = []

        for t in request.transacciones:
            categoria = predict_categoria(t, model, scaler, label_encoder)
            clasificadas.append(TransaccionClasificada(**t.model_dump(), categoria=categoria))

        return CategoriaResponse(
            success=True,
            message="transacciones clasificadas correctamente",
            data=CategoriaData(transacciones_clasificadas=clasificadas),
        )
    except Exception as e:
        logger.error(f"Error al predecir categoría: {e}")
        raise HTTPException(status_code=500, detail="Error interno del servidor")

# EndPoint para calcular el perfil financiero del usuario
@app.post("/perfil-financiero", response_model=PerfilFinancieroResponse)
def perfil_financiero(request: PerfilFinancieroRequest) -> PerfilFinancieroResponse:
    try:
        financial_stability_model = get_model("modelo_financial_stability")
        perfil_financiero_model = get_model("modelo_perfil_financiero")

        clasificadas = request.transacciones

        indicadores, resumen_gastos = compute_indicadores(request, clasificadas)
        score = predict_score_financiero(request, indicadores, financial_stability_model)
        perfil = predict_perfil_financiero(request, indicadores, perfil_financiero_model)
        feat = features(indicadores,resumen_gastos,score)
        perfil_features = perfil_desde_features(feat)
        reglas = evaluar_perfil_final(perfil_features)

        # Convertir Regla -> string
        recomendaciones = [
            regla.recomendacion
            for regla in reglas
        ]

        return PerfilFinancieroResponse(
            success=True,
            message="Perfil financiero obtenido correctamente",
            data=PerfilFinancieroData(
                perfil_financiero=perfil,
                score_financiero=score,
                resumen_gastos=resumen_gastos,
                indicadores=indicadores,
                transacciones_clasificadas=clasificadas,
                recomendaciones = recomendaciones
            ),
        )
    except Exception as exc:
        logger.exception("Error calculando perfil financiero")
        raise HTTPException(status_code=500, detail=str(exc))