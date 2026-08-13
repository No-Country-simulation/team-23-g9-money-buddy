from dataclasses import dataclass
from typing import Callable, Dict, List, Any, Optional


NIVELES_AHORRO_BAJO = {"Baja", "Nula"}
NIVELES_AHORRO_MEDIO_ALTO = {"Media", "Alta"}

_map_ahorro = {'Nula': 0.0, 'Baja': 0.03, 'Media': 0.1, 'Alta': 0.2}


def map_ahorro(s):
    return _map_ahorro.get(s, 0.0)


def cap(x, c):
    return x if x <= c else c


def features(indicadores, resumen_gastos, score) -> dict[str, Any]:
    ingreso=float(indicadores.get('ingreso_mensual',0))
    gasto=float(indicadores.get('gasto_total',0))
    return {
      'gasto_total':gasto,
      'porcentajes': {k: round((v/gasto)*100,2) for k,v in resumen_gastos.items()},
      'gasto_ingreso_ratio':gasto/ingreso,
      'gasto_ingreso_ratio_norm':round(cap(gasto/ingreso,1.5)/1.5,4),
      'ahorro_ratio':map_ahorro(indicadores.get('frecuencia_ahorro','Nulo')),
      'nivel_endeudamiento':float(indicadores.get('nivel_endeudamiento',0)),
      'nivel_endeudamiento_norm':round(cap(float(indicadores.get('nivel_endeudamiento',0))/100,1.0),4),
      'variabilidad_gasto':float(indicadores.get('variabilidad_gasto',0.0)),
      'pagos_recurrentes_sin_revision':int(indicadores.get('pagos_recurrentes_sin_revision',0)),
      'suscripciones_recurrentes_count':int(indicadores.get('suscripciones_recurrentes_count',0)),
      'deuda_tarjeta_interes_alto':bool(indicadores.get('deuda_tarjeta_interes_alto',False)),
      'ingreso_mensual':ingreso,
      'financial_stability_score':float(score)
    }


def ratio_a_nivel_ahorro(ratio: float) -> str:
    if ratio <= 0.015:      # equivale a 'Nula' (0.0)
        return "Nula"
    if ratio <= 0.065:      # equivale a 'Baja' (0.03)
        return "Baja"
    if ratio <= 0.15:       # equivale a 'Media' (0.1)
        return "Media"
    return "Alta"           # equivale a 'Alta' (0.2)


def perfil_desde_features(feat: Dict[str, Any]) -> Dict[str, Any]:

    porcentajes = feat.get("porcentajes", {}) or {}
    porcentajes_normalizados = {
        str(k).strip().lower(): float(v)
        for k, v in porcentajes.items()
    }
    return {
        "ingreso_mensual": feat.get("ingreso_mensual", 0.0),
        "gasto_ingreso_ratio": feat.get("gasto_ingreso_ratio", 0.0),
        "nivel_endeudamiento": feat.get("nivel_endeudamiento", 0.0),
        "score_financiero": feat.get("financial_stability_score", 0.0),
        "pct_alimentacion": porcentajes_normalizados.get("alimentacion", 0.0),
        "pct_ocio": porcentajes_normalizados.get("entretenimiento", 0.0),
        "pct_transporte": porcentajes_normalizados.get("transporte", 0.0),
        "pct_salud": porcentajes_normalizados.get("salud", 0.0),
        "pct_compras": porcentajes_normalizados.get("compras", 0.0),
        "pct_viaje": porcentajes_normalizados.get("viaje", 0.0),
        "pct_facturas": porcentajes_normalizados.get("facturas", 0.0),
        "pct_servicios": porcentajes_normalizados.get("servicios", 0.0),
        "pct_vivienda": porcentajes_normalizados.get("vivienda", 0.0),
        "pct_educacion": porcentajes_normalizados.get("educacion", 0.0),
        "pct_otro": porcentajes_normalizados.get("otro", 0.0),
        "pct_salario": porcentajes_normalizados.get("salario", 0.0),
        "pct_inversion": porcentajes_normalizados.get("inversion", 0.0),
        "nivel_ahorro": ratio_a_nivel_ahorro(feat.get("ahorro_ratio", 0.0)),
        "variabilidad_gasto": feat.get("variabilidad_gasto", 0.0),
        "num_suscripciones": feat.get("suscripciones_recurrentes_count", 0),
        "num_pagos_recurrentes_sin_revision": feat.get("pagos_recurrentes_sin_revision", 0),
        "deuda_tarjeta_interes_alto": feat.get("deuda_tarjeta_interes_alto", False)
    }


@dataclass
class Regla:
    id: str
    nombre: str
    condicion: Callable[[Dict[str, Any]], bool]
    recomendacion: str
    prioridad: str       
    impacto_estimado: str
    grupo: Optional[str] = None

    def evaluar(self, perfil: Dict[str, Any]) -> bool:
        try:
            return bool(self.condicion(perfil))
        except (KeyError, TypeError):
            return False


REGLAS: List[Regla] = []


def _registrar(regla: Regla) -> Regla:
    REGLAS.append(regla)
    return regla


R1 = _registrar(Regla(
    id="R1",
    nombre="Deuda critica",
    condicion=lambda p: (p["gasto_ingreso_ratio"] >= 0.9 and p["nivel_endeudamiento"] >= 40)
    or p.get("deuda_tarjeta_interes_alto", False),
    recomendacion="Pagar extra en la tarjeta o deuda con mayor saldo/tasa; recortar el gasto en ocio un 10% este mes.",
    prioridad="Alta",
    impacto_estimado="6-10% del ingreso mensual (menor costo por intereses)",
    grupo="deuda",
))

R2 = _registrar(Regla(
    id="R2",
    nombre="Sin ahorro",
    condicion=lambda p: p["nivel_ahorro"] == "Nulo" and p["score_financiero"] < 50,
    recomendacion="Automatizar el 5% del ingreso mensual hacia una cuenta de ahorro separada.",
    prioridad="Alta",
    impacto_estimado="ahorro inicial 3-5% del ingreso mensual",
    grupo=None,
))

R3 = _registrar(Regla(
    id="R3",
    nombre="Alimentacion elevada",
    condicion=lambda p: p["pct_alimentacion"] >= 25,
    recomendacion="Planificar el menu semanal con lista de compras fija; meta de bajar a 18% del gasto total en 8 semanas.",
    prioridad="Media",
    impacto_estimado="reduccion 4-8% del ingreso mensual",
    grupo=None,
))

R4 = _registrar(Regla(
    id="R4",
    nombre="Suscripciones multiples",
    condicion=lambda p: p["num_suscripciones"] >= 3,
    recomendacion="Revisar el listado completo de suscripciones activas y cancelar 1-2 no esenciales hoy mismo.",
    prioridad="Media",
    impacto_estimado="ahorro 1-4% del ingreso mensual",
    grupo="pagos_recurrentes",
))

R5 = _registrar(Regla(
    id="R5",
    nombre="Ocio alto y ahorro bajo",
    condicion=lambda p: p["pct_ocio"] >= 12 and p["nivel_ahorro"] in NIVELES_AHORRO_BAJO,
    recomendacion="Limitar el gasto en ocio este mes; pausar la contratacion de nuevas suscripciones.",
    prioridad="Media",
    impacto_estimado="ahorro 3-6% del ingreso mensual",
    grupo="gasto_discrecional",
))

R6 = _registrar(Regla(
    id="R6",
    nombre="Transporte elevado",
    condicion=lambda p: p["pct_transporte"] >= 15,
    recomendacion="Comparar el costo semanal actual de transporte contra alternativas (transporte publico) y probarlas 2 semanas.",
    prioridad="Baja",
    impacto_estimado="ahorro 2-5% del ingreso mensual",
    grupo=None,
))

R7 = _registrar(Regla(
    id="R7",
    nombre="Ahorro alto y deuda baja",
    condicion=lambda p: p["nivel_ahorro"] == "Alto" and p["nivel_endeudamiento"] < 20,
    recomendacion="Asignar el 50% del excedente mensual de ahorro a un instrumento conservador o a una meta financiera especifica.",
    prioridad="Media",
    impacto_estimado="mejor rendimiento del excedente a mediano plazo",
    grupo="inversion",
))

R8 = _registrar(Regla(
    id="R8",
    nombre="Variabilidad de gasto alta",
    condicion=lambda p: p["variabilidad_gasto"] > 0.25 and p["nivel_ahorro"] in NIVELES_AHORRO_BAJO,
    recomendacion="Programar una transferencia automatica semanal de un monto fijo hacia la cuenta de ahorro.",
    prioridad="Media",
    impacto_estimado="menor volatilidad en el flujo de caja",
    grupo=None,
))

R10 = _registrar(Regla(
    id="R10",
    nombre="Pagos recurrentes sin revision",
    condicion=lambda p: p["num_pagos_recurrentes_sin_revision"] >= 3,
    recomendacion="Revisar las facturas de los pagos recurrentes de este mes y negociar o cambiar de proveedor donde convenga.",
    prioridad="Media",
    impacto_estimado="ahorro 1-5% del ingreso mensual",
    grupo="pagos_recurrentes",
))

R11 = _registrar(Regla(
    id="R11",
    nombre="Gasto discrecional alto",
    condicion=lambda p: p["pct_gasto_discrecional"] >= 30,
    recomendacion="Fijar un tope de gasto discrecional semanal y usar subcuentas para monitorear el cumplimiento.",
    prioridad="Media",
    impacto_estimado="ahorro 5-10% del ingreso mensual",
    grupo="gasto_discrecional",
))

R12 = _registrar(Regla(
    id="R12",
    nombre="Fondo de emergencia insuficiente",
    condicion=lambda p: p["meses_fondo_emergencia"] < 3 and p["nivel_ahorro"] != "Nulo",
    recomendacion="Redirigir el 50% del ahorro mensual hacia el fondo de emergencia hasta alcanzar 3 meses de gasto.",
    prioridad="Alta",
    impacto_estimado="mayor resiliencia financiera; objetivo 3 meses de gasto",
    grupo=None,
))

R13 = _registrar(Regla(
    id="R13",
    nombre="Buen score y capacidad de inversion",
    condicion=lambda p: p["score_financiero"] >= 75 and p["nivel_ahorro"] == "Alto",
    recomendacion="Elaborar un plan de inversion con aportes periodicos definidos y revision de desempeno anual.",
    prioridad="Baja",
    impacto_estimado="optimizacion del rendimiento del ahorro a mediano plazo",
    grupo="inversion",
))

R14 = _registrar(Regla(
    id="R14",
    nombre="Gasto en salud no planificado",
    condicion=lambda p: p["pct_salud"] >= 12 and p["variabilidad_gasto"] > 0.2,
    recomendacion="Reservar un porcentaje fijo del ingreso mensual hasta acumular 1-2 meses de gasto medico.",
    prioridad="Media",
    impacto_estimado="menor riesgo de desbalance por gastos medicos",
    grupo=None,
))

R15 = _registrar(Regla(
    id="R15",
    nombre="Optimizacion fiscal",
    condicion=lambda p: p["ingreso_mensual"] >= 5000 and p["nivel_ahorro"] in NIVELES_AHORRO_MEDIO_ALTO,
    recomendacion="Revisar con asesoria profesional los instrumentos de ahorro con beneficios fiscales disponibles y ajustar aportes.",
    prioridad="Baja",
    impacto_estimado="posible ahorro fiscal anual segun jurisdiccion",
    grupo=None,
))

R16 = _registrar(Regla(
    id="R16",
    nombre="Ahorro nulo pese a buen score",
    condicion=lambda p: p["nivel_ahorro"] == "Nulo" and p["score_financiero"] >= 50 and p["meses_fondo_emergencia"] < 3,
    recomendacion="Automatizar un ahorro inicial del 5% del ingreso para empezar a construir el fondo de emergencia.",
    prioridad="Alta",
    impacto_estimado="ahorro inicial 3-5% del ingreso mensual",
    grupo=None,
))


PRIORIDAD_ORDEN = {"Alta": 0, "Media": 1, "Baja": 2}


def evaluar_perfil(perfil: Dict[str, Any]) -> List[Regla]:
    disparadas = [r for r in REGLAS if r.evaluar(perfil)]
    return sorted(disparadas, key=lambda r: PRIORIDAD_ORDEN[r.prioridad])


def evaluar_perfil_agrupado(perfil: Dict[str, Any]) -> List[Regla]:
    disparadas = evaluar_perfil(perfil)
    mejor_por_grupo: Dict[str, Regla] = {}
    sin_grupo: List[Regla] = []

    for regla in disparadas:
        if regla.grupo is None:
            sin_grupo.append(regla)
            continue
        actual = mejor_por_grupo.get(regla.grupo)
        if actual is None or PRIORIDAD_ORDEN[regla.prioridad] < PRIORIDAD_ORDEN[actual.prioridad]:
            mejor_por_grupo[regla.grupo] = regla

    resultado = list(mejor_por_grupo.values()) + sin_grupo
    return sorted(resultado, key=lambda r: PRIORIDAD_ORDEN[r.prioridad])


def evaluar_perfil_final(perfil: Dict[str, Any], max_resultados: int = 5) -> List[Regla]:
    max_resultados = max(3, min(5, max_resultados))
    return evaluar_perfil_agrupado(perfil)[:max_resultados]


