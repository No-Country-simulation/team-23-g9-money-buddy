package com.moneybuddy.transactions.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record TransaccionesRequest(
    @Valid
    @NotEmpty(message = "La lista de transacciones es obligatoria y no puede estar vacía")
    @JsonProperty("transacciones") List<@NotNull(message = "La transacción no puede ser nula") @Valid TransaccionRequest> transacciones) {

    public TransaccionesRequest {
        transacciones = transacciones == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(transacciones));
    }

    @ValidTransaction
    public record TransaccionRequest(
        @NotBlank(message = "El tipo de transacción es obligatorio")
        @Pattern(regexp = "Ingreso|Egreso", message = "El tipo de transacción debe ser Ingreso o Egreso")
        @JsonProperty("tipo") String tipo,
        @NotNull(message = "La fecha de la transacción es obligatoria")
        @JsonProperty("fecha") LocalDate fecha,
        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 200, message = "La descripción no puede contener más de 200 caracteres")
        @JsonProperty("descripcion") String descripcion,
        @JsonProperty("tipo_pago") String tipoPago,
        @Positive(message = "Los meses a deber deben ser mayores que cero")
        @JsonDeserialize(using = StrictIntegerDeserializer.class)
        @JsonProperty("meses_a_deber") Integer mesesADeber,
        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser mayor que cero")
        @JsonDeserialize(using = StrictBigDecimalDeserializer.class)
        @JsonProperty("monto") BigDecimal monto) {
    }

    static final class StrictBigDecimalDeserializer extends JsonDeserializer<BigDecimal> {

        @Override
        public BigDecimal deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            JsonNode node = parser.readValueAsTree();
            if (!node.isNumber()) {
                throw JsonMappingException.from(parser, "El monto debe ser numérico");
            }
            return node.decimalValue();
        }
    }

    static final class StrictIntegerDeserializer extends JsonDeserializer<Integer> {

        @Override
        public Integer deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            JsonNode node = parser.readValueAsTree();
            if (!node.isIntegralNumber()) {
                throw JsonMappingException.from(parser, "Los meses a deber deben ser numéricos");
            }
            return node.intValue();
        }
    }
}
