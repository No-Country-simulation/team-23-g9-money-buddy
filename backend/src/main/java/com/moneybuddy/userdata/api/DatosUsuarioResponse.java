package com.moneybuddy.userdata.api;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;

public record DatosUsuarioResponse(
    @JsonProperty("success") boolean success,
    @JsonProperty("message") String message,
    @JsonProperty("data") Data data ) {
    public record Data(
        @JsonProperty("credito_total") BigDecimal creditoTotal,
        @JsonProperty("ingreso_mensual") BigDecimal ingresoMensual,
        @JsonProperty("frecuencia_ahorro") String frecuenciaAhorro) {
    }
}
