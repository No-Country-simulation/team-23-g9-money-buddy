package com.moneybuddy.userdata.api;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "Datos del perfil financiero del usuario. Esta solicitud del MVP se valida sin persistencia.")
public record DatosUsuarioRequest(
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
    @JsonProperty("frecuencia_ahorro") String frecuenciaAhorro ) {

}
