package com.moneybuddy.analysis.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record AnalisisFinancieroResponse(
    @JsonProperty("estado") String estado,
    @JsonProperty("resumen") Resumen resumen,
    @JsonProperty("indicadores") Indicadores indicadores,
    @JsonProperty("resumen_gastos") Map<String, BigDecimal> resumenGastos,
    @JsonProperty("recomendaciones") List<String> recomendaciones) {

  public record Resumen(
      @JsonProperty("salud_financiera") String saludFinanciera,
      @JsonProperty("flujo_mensual_estimado") BigDecimal flujoMensualEstimado,
      @JsonProperty("total_transacciones") int totalTransacciones) {
  }

  public record Indicadores(
      @JsonProperty("tasa_ahorro") BigDecimal tasaAhorro,
      @JsonProperty("ratio_pago_deudas") BigDecimal ratioPagoDeudas,
      @JsonProperty("ratio_deuda_ingreso") BigDecimal ratioDeudaIngreso,
      @JsonProperty("nivel_ahorro") String nivelAhorro,
      @JsonProperty("nivel_deuda") String nivelDeuda,
      @JsonProperty("gasto_total") BigDecimal gastoTotal) {
  }
}
