package com.moneybuddy.transactions.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TransaccionesResponse(
    @JsonProperty("success") boolean success,
    @JsonProperty("message") String message,
    @JsonProperty("data") Data data) {

    public record Data(
        @JsonProperty("transacciones_clasificadas") List<TransaccionClasificada> transaccionesClasificadas) {
    }

    public record TransaccionClasificada(
        @JsonProperty("tipo") String tipo,
        @JsonProperty("fecha") LocalDate fecha,
        @JsonProperty("descripcion") String descripcion,
        @JsonProperty("tipo_pago") String tipoPago,
        @JsonProperty("meses_a_deber") Integer mesesADeber,
        @JsonProperty("monto") BigDecimal monto,
        @JsonProperty("categoria") String categoria) {
    }
}
