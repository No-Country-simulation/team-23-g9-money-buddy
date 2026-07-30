package com.moneybuddy.analysis.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
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

@Schema(description = "Solicitud sin estado de análisis financiero")
public record AnalisisFinancieroRequest(
	@NotNull(message = "El crédito total es obligatorio")
    @PositiveOrZero(message = "El crédito total debe ser mayor o igual que cero")
    @Schema(description = "Límite total de crédito del usuario.", example = "15000", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("credito_total") BigDecimal creditoTotal,
    @NotNull(message = "El ingreso mensual es obligatorio")
    @Positive(message = "El ingreso mensual debe ser mayor que cero")
    @Schema(description = "Ingreso mensual del usuario.", example = "4500", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("ingreso_mensual") BigDecimal ingresoMensual,
    @NotBlank(message = "La frecuencia de ahorro es obligatoria")
    @Pattern(regexp = "(?i)NULA|BAJA|MEDIA|ALTA", message = "La frecuencia de ahorro debe ser NULA, BAJA, MEDIA o ALTA")
    @Schema(description = "Frecuencia de ahorro. Los valores aceptados son NULA, BAJA, MEDIA o ALTA.", allowableValues = {"NULA", "BAJA", "MEDIA", "ALTA"}, example = "MEDIA", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("frecuencia_ahorro") String frecuenciaAhorro,
    @NotNull(message = "El pago mensual de deudas es obligatorio")
    @PositiveOrZero(message = "El pago mensual de deudas debe ser mayor o igual que cero")
    @Schema(description = "Monto mensual destinado al pago de deudas existentes.", example = "600", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("pago_mensual_deudas") BigDecimal pagoMensualDeudas,
    @Valid
    @NotEmpty(message = "La lista de transacciones es obligatoria y no puede estar vacía")
    @Size(max = 100, message = "La lista de transacciones no puede contener más de 100 elementos")
    @ArraySchema(schema = @Schema(implementation = TransaccionRequest.class), minItems = 1, maxItems = 100)
    @JsonProperty("transacciones") List<TransaccionRequest> transacciones) {

	public AnalisisFinancieroRequest {
		transacciones = transacciones == null ? List.of() : List.copyOf(transacciones);
  	}

	@ValidFinancialTransaction
    @Schema(description = "Transacción enviada para el análisis financiero. tipo_pago es obligatorio solo para Egreso; meses_a_deber es obligatorio cuando tipo_pago es Credito.")
	public record TransaccionRequest(
    	@NotBlank(message = "El tipo de transacción es obligatorio")
    	@Pattern(regexp = "(?i)Ingreso|Egreso", message = "El tipo de transacción debe ser Ingreso o Egreso")
        @Schema(description = "Tipo de transacción.", allowableValues = {"Ingreso", "Egreso"}, example = "Egreso", requiredMode = Schema.RequiredMode.REQUIRED)
      	@JsonProperty("tipo") String tipo,
      	@NotNull(message = "La fecha de la transacción es obligatoria")
        @Schema(description = "Fecha de la transacción en formato ISO-8601.", example = "2026-07-05", requiredMode = Schema.RequiredMode.REQUIRED)
      	@JsonProperty("fecha") LocalDate fecha,
      	@NotBlank(message = "La descripción es obligatoria")
      	@Size(max = 200, message = "La descripción no puede contener más de 200 caracteres")
        @Schema(description = "Descripción de la transacción, de hasta 200 caracteres.", example = "Supermercado", requiredMode = Schema.RequiredMode.REQUIRED)
      	@JsonProperty("descripcion") String descripcion,
		@Schema(description = "Tipo de pago. Obligatorio solo cuando tipo es Egreso. Los valores aceptados son Efectivo, Debito y Credito.", allowableValues = {"Efectivo", "Debito", "Credito"}, example = "Debito")
		@JsonProperty("tipo_pago") String tipoPago,
      	@Positive(message = "Los meses a deber deben ser mayores que cero")
        @Schema(description = "Cantidad de meses a deber. Obligatorio cuando tipo_pago es Credito.", example = "12")
      	@JsonProperty("meses_a_deber") Integer mesesADeber,
      	@NotNull(message = "El monto es obligatorio")
      	@Positive(message = "El monto debe ser mayor que cero")
        @Schema(description = "Monto de la transacción. Debe ser mayor que cero.", example = "350", requiredMode = Schema.RequiredMode.REQUIRED)
      	@JsonProperty("monto") BigDecimal monto) {
	}
}
