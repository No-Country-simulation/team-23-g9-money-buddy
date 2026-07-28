package com.moneybuddy.userdata.api;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record DatosUsuarioRequest(
    @NotNull(message = "El crédito total es obligatorio")
    @PositiveOrZero(message = "El crédito total debe ser mayor o igual que cero")
    @JsonProperty("credito_total") BigDecimal creditoTotal,
    @NotNull(message = "El ingreso mensual es obligatorio")
    @Positive(message = "El ingreso mensual debe ser mayor que cero")
    @JsonProperty("ingreso_mensual") BigDecimal ingresoMensual,
    @NotBlank(message = "La frecuencia de ahorro es obligatoria")
    @Pattern(regexp = "(?i)NULA|BAJA|MEDIA|ALTA", message = "La frecuencia de ahorro debe ser NULA, BAJA, MEDIA o ALTA")
    @JsonProperty("frecuencia_ahorro") String frecuenciaAhorro ) {

}
