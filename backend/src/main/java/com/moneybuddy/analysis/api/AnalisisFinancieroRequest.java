package com.moneybuddy.analysis.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AnalisisFinancieroRequest(
    @JsonProperty("ingreso_mensual") BigDecimal ingresoMensual,
    @JsonProperty("ahorro_mensual") BigDecimal ahorroMensual,
    @JsonProperty("deuda_total") BigDecimal deudaTotal,
    @JsonProperty("pago_mensual_deudas") BigDecimal pagoMensualDeudas,
    @JsonProperty("transacciones") List<TransaccionRequest> transacciones) {

  public AnalisisFinancieroRequest {
    transacciones = transacciones == null ? List.of() : List.copyOf(transacciones);
  }

  public record TransaccionRequest(
      @JsonProperty("descripcion") String descripcion,
      @JsonProperty("categoria") String categoria,
      @JsonProperty("monto") BigDecimal monto,
      @JsonProperty("fecha") LocalDate fecha,
      @JsonProperty("tipo") String tipo) {
  }
}
