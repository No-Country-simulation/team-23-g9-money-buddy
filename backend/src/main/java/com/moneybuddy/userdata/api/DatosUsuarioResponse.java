package com.moneybuddy.userdata.api;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta con los datos financieros del usuario validados.")
public record DatosUsuarioResponse(
    @Schema(description = "Indica si la solicitud se procesó correctamente.", example = "true")
    @JsonProperty("success") boolean success,
    @Schema(description = "Mensaje de resultado legible.", example = "Datos financieros validados correctamente")
    @JsonProperty("message") String message,
    @Schema(description = "Datos validados devueltos por el endpoint sin estado.")
    @JsonProperty("data") Data data ) {
    @Schema(description = "Datos financieros validados.")
    public record Data(
        @Schema(description = "Límite total de crédito aprobado.", example = "15000")
        @JsonProperty("credito_total") BigDecimal creditoTotal,
        @Schema(description = "Ingreso mensual.", example = "4500")
        @JsonProperty("ingreso_mensual") BigDecimal ingresoMensual,
        @Schema(description = "Frecuencia de ahorro.", example = "MEDIA")
        @JsonProperty("frecuencia_ahorro") String frecuenciaAhorro) {
    }
}
