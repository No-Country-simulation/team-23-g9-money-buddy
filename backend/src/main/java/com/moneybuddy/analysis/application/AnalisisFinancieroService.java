package com.moneybuddy.analysis.application;

import com.moneybuddy.analysis.api.AnalisisFinancieroRequest;
import com.moneybuddy.analysis.api.AnalisisFinancieroResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public final class AnalisisFinancieroService {

  private static final BigDecimal DIEZ_POR_CIENTO = new BigDecimal("0.10");
  private static final BigDecimal VEINTE_POR_CIENTO = new BigDecimal("0.20");
  private static final BigDecimal TREINTA_Y_CINCO_POR_CIENTO = new BigDecimal("0.35");
  private static final BigDecimal TRES_VECES_INGRESO = new BigDecimal("3.00");
  private static final BigDecimal SEIS_VECES_INGRESO = new BigDecimal("6.00");

  public AnalisisFinancieroResponse analizar(AnalisisFinancieroRequest request) {
    BigDecimal ingresoMensual = valueOrZero(request.ingresoMensual());
    BigDecimal ahorroMensual = valueOrZero(request.ahorroMensual());
    BigDecimal deudaTotal = valueOrZero(request.deudaTotal());
    BigDecimal pagoMensualDeudas = valueOrZero(request.pagoMensualDeudas());

    BigDecimal tasaAhorro = ratio(ahorroMensual, ingresoMensual);
    BigDecimal ratioPagoDeudas = ratio(pagoMensualDeudas, ingresoMensual);
    BigDecimal ratioDeudaIngreso = ratio(deudaTotal, ingresoMensual);
    BigDecimal flujoMensualEstimado = ingresoMensual.subtract(ahorroMensual).subtract(pagoMensualDeudas);

    String nivelAhorro = nivelAhorro(tasaAhorro);
    String nivelDeuda = nivelDeuda(ratioPagoDeudas, ratioDeudaIngreso);
    String saludFinanciera = saludFinanciera(nivelAhorro, nivelDeuda, flujoMensualEstimado);

    return new AnalisisFinancieroResponse(
        "analisis_generado",
        new AnalisisFinancieroResponse.Resumen(
            saludFinanciera,
            flujoMensualEstimado,
            request.transacciones().size()),
        new AnalisisFinancieroResponse.Indicadores(
            tasaAhorro,
            ratioPagoDeudas,
            ratioDeudaIngreso,
            nivelAhorro,
            nivelDeuda),
        recomendaciones(nivelAhorro, nivelDeuda, flujoMensualEstimado));
  }

  private BigDecimal valueOrZero(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  private BigDecimal ratio(BigDecimal numerator, BigDecimal denominator) {
    if (denominator.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
    }

    return numerator.divide(denominator, 4, RoundingMode.HALF_UP);
  }

  private String nivelAhorro(BigDecimal tasaAhorro) {
    if (tasaAhorro.compareTo(VEINTE_POR_CIENTO) >= 0) {
      return "saludable";
    }
    if (tasaAhorro.compareTo(DIEZ_POR_CIENTO) >= 0) {
      return "moderado";
    }
    return "bajo";
  }

  private String nivelDeuda(BigDecimal ratioPagoDeudas, BigDecimal ratioDeudaIngreso) {
    if (ratioPagoDeudas.compareTo(TREINTA_Y_CINCO_POR_CIENTO) > 0
        || ratioDeudaIngreso.compareTo(SEIS_VECES_INGRESO) > 0) {
      return "critica";
    }
    if (ratioPagoDeudas.compareTo(VEINTE_POR_CIENTO) > 0 || ratioDeudaIngreso.compareTo(TRES_VECES_INGRESO) > 0) {
      return "elevada";
    }
    return "controlada";
  }

  private String saludFinanciera(String nivelAhorro, String nivelDeuda, BigDecimal flujoMensualEstimado) {
    if (flujoMensualEstimado.compareTo(BigDecimal.ZERO) < 0 || "critica".equals(nivelDeuda)) {
      return "requiere_atencion";
    }
    if ("saludable".equals(nivelAhorro) && "controlada".equals(nivelDeuda)) {
      return "estable";
    }
    return "requiere_mejora";
  }

  private List<String> recomendaciones(String nivelAhorro, String nivelDeuda, BigDecimal flujoMensualEstimado) {
    List<String> recomendaciones = new ArrayList<>();

    if ("bajo".equals(nivelAhorro)) {
      recomendaciones.add("Aumenta el ahorro mensual hasta al menos el 10% de tus ingresos.");
    } else {
      recomendaciones.add("Mantén protegido el hábito de ahorro actual.");
    }

    if ("critica".equals(nivelDeuda) || "elevada".equals(nivelDeuda)) {
      recomendaciones.add("Prioriza el pago de deudas con intereses altos antes de aumentar gastos discrecionales.");
    } else {
      recomendaciones.add("La presión de deuda está controlada; evita asumir nuevas obligaciones recurrentes.");
    }

    if (flujoMensualEstimado.compareTo(BigDecimal.ZERO) < 0) {
      recomendaciones.add("Revisa los gastos fijos porque el flujo mensual estimado es negativo.");
    }

    return List.copyOf(recomendaciones);
  }
}
