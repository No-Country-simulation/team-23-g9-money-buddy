package com.moneybuddy.analysis.application;

import com.moneybuddy.analysis.api.AnalisisFinancieroRequest;
import com.moneybuddy.analysis.api.AnalisisFinancieroResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public final class AnalisisFinancieroService {

	private static final BigDecimal CIEN = new BigDecimal("100.00");
	private static final List<String> CATEGORIAS_EGRESO = List.of(
		"alimentos",
		"transporte",
		"ocio_entretenimiento",
		"salud",
		"vivienda",
		"educacion",
		"viajes",
		"servicios",
		"compras",
		"otros");

  	public AnalisisFinancieroResponse analizar(AnalisisFinancieroRequest request) {
    	BigDecimal ingresoMensual = valorOCero(request.ingresoMensual());
    	BigDecimal creditoTotal = valorOCero(request.creditoTotal());
    	BigDecimal pagoMensualDeudas = valorOCero(request.pagoMensualDeudas());
    	List<AnalisisFinancieroResponse.TransaccionClasificada> transaccionesClasificadas = clasificarTransacciones(
		request.transacciones());

		BigDecimal deudaTotal = totalPorCrédito(transaccionesClasificadas);
		BigDecimal gastoTotal = totalPorTipo(transaccionesClasificadas, "Egreso");
		Map<String, BigDecimal> resumenGastos = resumenGastos(transaccionesClasificadas);
		Map<String, BigDecimal> porcentajeCategorias = porcentajeCategorias(resumenGastos, gastoTotal);
		BigDecimal nivelEndeudamiento = porcentaje(deudaTotal, creditoTotal);
		BigDecimal ratioPagoDeudas = ratio(pagoMensualDeudas, ingresoMensual);
		BigDecimal ratioDeudaIngreso = ratio(deudaTotal, ingresoMensual);
		String frecuenciaAhorro = normalizarFrecuenciaDeAhorro(request.frecuenciaAhorro());
		int scoreFinanciero = scoreFinanciero(
			frecuenciaAhorro, ratioPagoDeudas, ratioDeudaIngreso, nivelEndeudamiento);
		String perfilFinanciero = perfilFinanciero(scoreFinanciero, ratioPagoDeudas, nivelEndeudamiento);

		return new AnalisisFinancieroResponse(
			true,
			"Análisis financiero generado exitosamente",
			new AnalisisFinancieroResponse.Data(
				perfilFinanciero,
				scoreFinanciero,
				resumenGastos,
				new AnalisisFinancieroResponse.Indicadores(
					ingresoMensual,
					deudaTotal,
					creditoTotal,
					nivelEndeudamiento,
					frecuenciaAhorro,
					pagoMensualDeudas,
					gastoTotal,
					ratioPagoDeudas,
					ratioDeudaIngreso,
					porcentajeCategorias),
				transaccionesClasificadas,
				recomendaciones(perfilFinanciero, frecuenciaAhorro, ratioPagoDeudas, nivelEndeudamiento)));
	}

  private List<AnalisisFinancieroResponse.TransaccionClasificada> clasificarTransacciones(
      List<AnalisisFinancieroRequest.TransaccionRequest> transacciones) {
    return transacciones.stream()
        .map(transaccion -> new AnalisisFinancieroResponse.TransaccionClasificada(
            normalizarTipo(transaccion.tipo()),
            transaccion.fecha(),
            transaccion.descripcion(),
            transaccion.tipoPago(),
            transaccion.mesesADeber(),
            valorOCero(transaccion.monto()),
            categoria(transaccion)))
        .toList();
  	}

	private BigDecimal totalPorCrédito(List<AnalisisFinancieroResponse.TransaccionClasificada> transacciones) {
		return transacciones.stream()
			.filter(transaccion -> "Egreso".equals(transaccion.tipo()))
			.filter(transaccion -> "Credito".equalsIgnoreCase(transaccion.tipoPago()))
			.map(AnalisisFinancieroResponse.TransaccionClasificada::monto)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private BigDecimal totalPorTipo(List<AnalisisFinancieroResponse.TransaccionClasificada> transacciones, String tipo) {
		return transacciones.stream()
			.filter(transaccion -> tipo.equals(transaccion.tipo()))
			.map(AnalisisFinancieroResponse.TransaccionClasificada::monto)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private Map<String, BigDecimal> resumenGastos(List<AnalisisFinancieroResponse.TransaccionClasificada> transacciones) {
		Map<String, BigDecimal> resumen = new LinkedHashMap<>();
		for (String categoria : CATEGORIAS_EGRESO) {
			resumen.put(categoria, BigDecimal.ZERO);
		}

		for (AnalisisFinancieroResponse.TransaccionClasificada transaccion : transacciones) {
			if (!"Egreso".equals(transaccion.tipo())) {
				continue;
			}

			resumen.merge(transaccion.categoria(), transaccion.monto(), BigDecimal::add);
		}

		return Collections.unmodifiableMap(resumen);
	}

	private Map<String, BigDecimal> porcentajeCategorias(Map<String, BigDecimal> resumenGastos, BigDecimal gastoTotal) {
		Map<String, BigDecimal> porcentajes = new LinkedHashMap<>();

		for (Map.Entry<String, BigDecimal> entry : resumenGastos.entrySet()) {
			porcentajes.put(entry.getKey(), porcentaje(entry.getValue(), gastoTotal));
		}

		return Collections.unmodifiableMap(porcentajes);
	}

	private String categoria(AnalisisFinancieroRequest.TransaccionRequest transaccion) {
		if ("Ingreso".equalsIgnoreCase(transaccion.tipo())) {
			return "ingreso";
		}

		String descripcion = normalizar(transaccion.descripcion());

		if (containsAny(descripcion, "supermercado", "mercado", "comida", "restaurante", "cafe", "delivery", "alimento")) {
			return "alimentos";
		}
		if (containsAny(descripcion, "bus", "taxi", "uber", "transporte", "metro", "combustible", "gasolina")) {
			return "transporte";
		}
		if (containsAny(descripcion, "farmacia", "medico", "salud", "hospital", "clinica")) {
			return "salud";
		}
		if (containsAny(descripcion, "alquiler", "renta", "hipoteca")) {
			return "vivienda";
		}
		if (containsAny(descripcion, "colegio", "universidad", "curso", "libro", "educacion")) {
			return "educacion";
		}
		if (containsAny(descripcion, "viaje", "hotel", "vuelo", "pasaje", "turismo")) {
			return "viajes";
		}
		if (containsAny(descripcion, "luz", "agua", "internet", "servicio", "telefono", "gas")) {
			return "servicios";
		}
		if (containsAny(descripcion, "compra", "tienda", "ropa", "zapato", "electronica")) {
			return "compras";
		}
		if (containsAny(descripcion, "cine", "streaming", "juego", "entretenimiento", "ocio")) {
			return "ocio_entretenimiento";
		}

		return "otros";
	}

	private int scoreFinanciero(
		String frecuenciaAhorro,
		BigDecimal ratioPagoDeudas,
		BigDecimal ratioDeudaIngreso,
		BigDecimal nivelEndeudamiento) {
			int score = switch (normalizarFrecuenciaDeAhorro(frecuenciaAhorro)) {
				case "ALTA" -> 85;
				case "MEDIA" -> 72;
				case "BAJA" -> 58;
				default -> 42;
			};

			if (ratioPagoDeudas.compareTo(new BigDecimal("0.30")) > 0) {
				score -= 15;
			}
			if (ratioDeudaIngreso.compareTo(new BigDecimal("0.50")) > 0) {
				score -= 10;
			}
			if (nivelEndeudamiento.compareTo(new BigDecimal("60.00")) > 0) {
				score -= 15;
			}

		return Math.max(0, Math.min(100, score));
  	}

	private String perfilFinanciero(int scoreFinanciero, BigDecimal ratioPagoDeudas, BigDecimal nivelEndeudamiento) {
		if (scoreFinanciero >= 75 && ratioPagoDeudas.compareTo(new BigDecimal("0.30")) <= 0) {
			return "estable";
		}
		if (scoreFinanciero < 50 || nivelEndeudamiento.compareTo(new BigDecimal("70.00")) > 0) {
			return "riesgo_alto";
		}
		return "requiere_atencion";
	}

	private List<String> recomendaciones(
		String perfilFinanciero,
		String frecuenciaAhorro,
		BigDecimal ratioPagoDeudas,
		BigDecimal nivelEndeudamiento) {
		List<String> recomendaciones = new ArrayList<>();

		if ("NULA".equals(normalizarFrecuenciaDeAhorro(frecuenciaAhorro)) || "BAJA".equals(normalizarFrecuenciaDeAhorro(frecuenciaAhorro))) {
			recomendaciones.add("Aumenta tu frecuencia de ahorro antes de agregar nuevos gastos recurrentes.");
		} else {
			recomendaciones.add("Mantén protegido tu hábito de ahorro actual.");
		}

		if (ratioPagoDeudas.compareTo(new BigDecimal("0.30")) > 0 || nivelEndeudamiento.compareTo(new BigDecimal("60.00")) > 0) {
			recomendaciones.add("Prioriza los pagos de crédito y evita asumir nuevas deudas.");
		} else {
			recomendaciones.add("Tu presión de deuda está controlada; sigue monitoreando los gastos de crédito.");
		}

		if ("riesgo_alto".equals(perfilFinanciero)) {
			recomendaciones.add("Revisa los gastos discrecionales y arma un plan de reducción de deuda a corto plazo.");
		}

		return List.copyOf(recomendaciones);
	}

	private BigDecimal valorOCero(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	private BigDecimal ratio(BigDecimal numerator, BigDecimal denominator) {
		if (denominator.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
		}

		return numerator.divide(denominator, 4, RoundingMode.HALF_UP);
	}

	private BigDecimal porcentaje(BigDecimal numerator, BigDecimal denominator) {
		return ratio(numerator, denominator).multiply(CIEN).setScale(2, RoundingMode.HALF_UP);
	}

	private String normalizarTipo(String tipo) {
		return "Ingreso".equalsIgnoreCase(tipo) ? "Ingreso" : "Egreso";
	}

	private String normalizarFrecuenciaDeAhorro(String frecuenciaAhorro) {
		return frecuenciaAhorro == null ? "NULA" : frecuenciaAhorro.toUpperCase(Locale.ROOT);
	}

	private String normalizar(String value) {
		return value == null ? "" : value.toLowerCase(Locale.ROOT);
	}

	private boolean containsAny(String value, String... candidates) {
		for (String candidate : candidates) {
			if (value.contains(candidate)) {
				return true;
			}
		}
		return false;
	}
}
