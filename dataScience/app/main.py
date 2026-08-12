import logging
from fastapi import FastAPI, HTTPException
from contextlib import asynccontextmanager

from .processing import predict_categoria, compute_indicadores, predict_score_financiero, predict_perfil_financiero, generate_recomendaciones
from .schemas import (
    CategoriaData,
    CategoriaRequest,
    CategoriaResponse,
    PerfilFinancieroData,
    PerfilFinancieroRequest,
    PerfilFinancieroResponse,
    TransaccionClasificada,
)
from .models import load_models, get_model

app = FastAPI( title="MoneyBuddy AI", version="1.0.0" )

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

# Comando para ver el estado de la API
@app.get("/health")
def health():
    return {"status": "ok", "modelos_cargados": list(get_model().keys())}

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
        financial_stability_model = get_model("rf_financial_stability")
        perfil_financiero_model = get_model("rf_perfil_financiero")

        clasificadas = []
        for t in request.transacciones:
            clasificadas.append(t)

        indicadores, resumen_gastos = compute_indicadores(request, clasificadas)
        score = predict_score_financiero(request, indicadores, financial_stability_model)
        perfil = predict_perfil_financiero(request, indicadores, perfil_financiero_model)
        #TODO: Implementar el motor de generar recomendaciones basado en el perfil financiero y los indicadores

        return PerfilFinancieroResponse(
            success=True,
            message="Perfil financiero obtenido correctamente",
            data=PerfilFinancieroData(
                perfil_financiero=perfil,
                score_financiero=score,
                resumen_gastos=resumen_gastos,
                indicadores=indicadores,
                transacciones_clasificadas=clasificadas,
            ),
        )
    except Exception as exc:
        logger.exception("Error calculando perfil financiero")
        raise HTTPException(status_code=500, detail=str(exc))