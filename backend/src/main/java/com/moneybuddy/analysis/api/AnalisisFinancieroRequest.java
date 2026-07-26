package com.moneybuddy.analysis.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AnalisisFinancieroRequest(
	@NotNull(message = "El crédito total es obligatorio")
    @PositiveOrZero(message = "El crédito total debe ser mayor o igual que cero")
    @JsonProperty("credito_total") BigDecimal creditoTotal,
    @NotNull(message = "El ingreso mensual es obligatorio")
    @Positive(message = "El ingreso mensual debe ser mayor que cero")
    @JsonProperty("ingreso_mensual") BigDecimal ingresoMensual,
    @NotBlank(message = "La frecuencia de ahorro es obligatoria")
    @Pattern(regexp = "(?i)NULA|BAJA|MEDIA|ALTA", message = "La frecuencia de ahorro debe ser NULA, BAJA, MEDIA o ALTA")
    @JsonProperty("frecuencia_ahorro") String frecuenciaAhorro,
    @NotNull(message = "El pago mensual de deudas es obligatorio")
    @PositiveOrZero(message = "El pago mensual de deudas debe ser mayor o igual que cero")
    @JsonProperty("pago_mensual_deudas") BigDecimal pagoMensualDeudas,
    @Valid
    @NotEmpty(message = "La lista de transacciones es obligatoria y no puede estar vacía")
    @Size(max = 100, message = "La lista de transacciones no puede contener más de 100 elementos")
    @JsonProperty("transacciones") List<TransaccionRequest> transacciones) {

	public AnalisisFinancieroRequest {
		transacciones = transacciones == null ? List.of() : List.copyOf(transacciones);
  	}

  	@ValidFinancialTransaction
  	public record TransaccionRequest(
    	@NotBlank(message = "El tipo de transacción es obligatorio")
    	@Pattern(regexp = "(?i)Ingreso|Egreso", message = "El tipo de transacción debe ser Ingreso o Egreso")
      	@JsonProperty("tipo") String tipo,
      	@NotNull(message = "La fecha de la transacción es obligatoria")
      	@JsonProperty("fecha") LocalDate fecha,
      	@NotBlank(message = "La descripción es obligatoria")
      	@Size(max = 200, message = "La descripción no puede contener más de 200 caracteres")
      	@JsonProperty("descripcion") String descripcion,
		@JsonProperty("tipo_pago") String tipoPago,
      	@Positive(message = "Los meses a deber deben ser mayores que cero")
      	@JsonProperty("meses_a_deber") Integer mesesADeber,
      	@NotNull(message = "El monto es obligatorio")
      	@Positive(message = "El monto debe ser mayor que cero")
      	@JsonProperty("monto") BigDecimal monto) {
  	}
}
