from typing import Optional

from pydantic import BaseModel

class Transaccion(BaseModel):
    tipo: str
    fecha: str
    descripcion: str
    tipoPago: str
    meses_a_deber: Optional[int] = None
    monto: float

class TransaccionClasificada(Transaccion):
    categoria: Optional[str] = None

class CategoriaRequest(BaseModel):
    transacciones: list[Transaccion]

# ---------- /predecir-categoria ----------

class CategoriaData(BaseModel):
    transacciones_clasificadas: list[TransaccionClasificada]


class CategoriaResponse(BaseModel):
    success: bool
    message: str
    data: CategoriaData

# ---------- /perfil-financiero ----------

class PerfilFinancieroRequest(BaseModel):
    credito_total: float
    ingreso_mensual: float
    frecuencia_ahorro: str
    nivel_endeudamiento: float
    pago_mensual_deudas: float
    transacciones: list[TransaccionClasificada]


class Indicadores(BaseModel):
    ingreso_mensual: float
    deuda_total: float
    credito_total: float
    frecuencia_ahorro: str
    nivel_endeudamiento: float
    pago_mensual_deudas: float
    gasto_total: float
    ratio_pago_deudas: float
    ratio_deuda_ingreso: float
    porcentaje_alimentos: float = 0
    porcentaje_transporte: float = 0
    porcentaje_entretenimiento: float = 0
    porcentaje_salud: float = 0
    porcentaje_vivienda: float = 0
    porcentaje_educacion: float = 0
    porcentaje_viajes: float = 0
    porcentaje_servicios: float = 0
    porcentaje_otros: float = 0


class PerfilFinancieroData(BaseModel):
    perfil_financiero: str
    score_financiero: float
    resumen_gastos: dict[str, float]
    indicadores: Indicadores
    transacciones_clasificadas: list[TransaccionClasificada]
    recomendaciones: list[str]


class PerfilFinancieroResponse(BaseModel):
    success: bool
    message: str
    data: PerfilFinancieroData
