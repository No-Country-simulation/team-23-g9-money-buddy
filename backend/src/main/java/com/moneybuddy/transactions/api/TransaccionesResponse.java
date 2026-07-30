package com.moneybuddy.transactions.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Respuesta de clasificación de transacciones generada por el backend.")
public record TransaccionesResponse(
    @Schema(description = "Indica si la solicitud se procesó correctamente.", example = "true")
    @JsonProperty("success") boolean success,
    @Schema(description = "Mensaje de resultado legible.", example = "Transacciones clasificadas correctamente")
    @JsonProperty("message") String message,
    @Schema(description = "Datos de transacciones.")
    @JsonProperty("data") Data data) {

    @Schema(description = "Contenedor de transacciones clasificadas.")
    public record Data(
        @ArraySchema(schema = @Schema(implementation = TransaccionClasificada.class))
        @JsonProperty("transacciones_clasificadas") List<TransaccionClasificada> transaccionesClasificadas) {
    }

    @Schema(description = "Transacción enriquecida con una categoría calculada por el backend.")
    public record TransaccionClasificada(
        @Schema(description = "Tipo de transacción.", example = "Egreso")
        @JsonProperty("tipo") String tipo,
        @Schema(description = "Fecha de la transacción.", example = "2026-07-05")
        @JsonProperty("fecha") LocalDate fecha,
        @Schema(description = "Descripción de la transacción.", example = "Supermercado")
        @JsonProperty("descripcion") String descripcion,
        @Schema(description = "Tipo de pago para transacciones de egreso.", example = "Debito")
        @JsonProperty("tipo_pago") String tipoPago,
        @Schema(description = "Meses a deber cuando el tipo de pago es crédito.", example = "12")
        @JsonProperty("meses_a_deber") Integer mesesADeber,
        @Schema(description = "Monto de la transacción.", example = "350")
        @JsonProperty("monto") BigDecimal monto,
        @Schema(description = "Categoría de la transacción calculada por el backend.", example = "alimentos")
        @JsonProperty("categoria") String categoria) {
    }
}
