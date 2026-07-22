package com.moneybuddy.analysis.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AnalisisFinancieroRequest(
    @NotNull(message = "El ingreso mensual es obligatorio")
    @Positive(message = "El ingreso mensual debe ser mayor que cero")
    @JsonProperty("ingreso_mensual") BigDecimal ingresoMensual,
    @NotNull(message = "El ahorro mensual es obligatorio")
    @PositiveOrZero(message = "El ahorro mensual debe ser mayor o igual que cero")
    @JsonProperty("ahorro_mensual") BigDecimal ahorroMensual,
    @NotNull(message = "La deuda total es obligatoria")
    @PositiveOrZero(message = "La deuda total debe ser mayor o igual que cero")
    @JsonProperty("deuda_total") BigDecimal deudaTotal,
    @NotNull(message = "El pago mensual de deudas es obligatorio")
    @PositiveOrZero(message = "El pago mensual de deudas debe ser mayor o igual que cero")
    @JsonProperty("pago_mensual_deudas") BigDecimal pagoMensualDeudas,
    @Valid
    @NotEmpty(message = "La lista de transacciones es obligatoria y no puede estar vacía")
    @JsonProperty("transacciones") List<TransaccionRequest> transacciones) {

  public AnalisisFinancieroRequest {
    transacciones = transacciones == null ? List.of() : List.copyOf(transacciones);
  }

  public record TransaccionRequest(
      @NotBlank(message = "La descripción es obligatoria y no puede estar vacía")
      @JsonProperty("descripcion") String descripcion,
      @NotBlank(message = "La categoría de la transacción es obligatoria")
      @JsonProperty("categoria") String categoria,
      @NotNull(message = "El monto de la transacción es obligatorio")
      @Positive(message = "El monto de la transacción debe ser mayor que cero")
      @JsonProperty("monto") BigDecimal monto,
      @NotNull(message = "La fecha de la transacción es obligatoria")
      @JsonProperty("fecha") LocalDate fecha,
      @NotBlank(message = "El tipo de transacción es obligatorio")
      @JsonProperty("tipo") String tipo) {
  }
}
