package com.moneybuddy.transactions.api;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransaccionesController.class)
@Import(TransaccionesService.class)
class TransaccionesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void postTransaccionesReturnsClassifiedTransactions() throws Exception {
        mockMvc.perform(post("/transacciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Transacciones clasificadas exitosamente"))
                .andExpect(jsonPath("$.data.transacciones_clasificadas", hasSize(3)))
                .andExpect(jsonPath("$.data.transacciones_clasificadas[0].tipo").value("Egreso"))
                .andExpect(jsonPath("$.data.transacciones_clasificadas[0].tipo_pago").value("Debito"))
                .andExpect(jsonPath("$.data.transacciones_clasificadas[0].categoria").value("alimentos"))
                .andExpect(jsonPath("$.data.transacciones_clasificadas[1].tipo_pago").value("Credito"))
                .andExpect(jsonPath("$.data.transacciones_clasificadas[1].meses_a_deber").value(3))
                .andExpect(jsonPath("$.data.transacciones_clasificadas[1].categoria").value("transporte"))
                .andExpect(jsonPath("$.data.transacciones_clasificadas[2].tipo").value("Ingreso"))
                .andExpect(jsonPath("$.data.transacciones_clasificadas[2].tipo_pago").doesNotExist())
                .andExpect(jsonPath("$.data.transacciones_clasificadas[2].categoria").value("ingreso"));
    }

    @Test
    void postTransaccionesRejectsEmptyTransactions() throws Exception {
        mockMvc.perform(post("/transacciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"transacciones\": [] }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value("request_invalido"))
                .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones')].mensaje")
                        .value("La lista de transacciones es obligatoria y no puede estar vacía"));
    }

    @Test
    void postTransaccionesRejectsMissingRequiredFields() throws Exception {
        String request = """
                {
                  "transacciones": [
                    {}
                  ]
                }
                """;

        mockMvc.perform(post("/transacciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value("request_invalido"))
                .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones[0].tipo')].mensaje")
                        .value("El tipo de transacción es obligatorio"))
                .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones[0].fecha')].mensaje")
                        .value("La fecha de la transacción es obligatoria"))
                .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones[0].descripcion')].mensaje")
                        .value("La descripción es obligatoria"))
                .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones[0].monto')].mensaje")
                        .value("El monto es obligatorio"));
    }

    @Test
    void postTransaccionesRejectsInvalidEnumAndAmountValues() throws Exception {
        String request = """
                {
                  "transacciones": [
                    {
                      "tipo": "Gasto",
                      "fecha": "2026-07-20",
                      "descripcion": "Taxi",
                      "tipo_pago": "Tarjeta",
                      "monto": 0
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/transacciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones[0].tipo')].mensaje")
                        .value("El tipo de transacción debe ser Ingreso o Egreso"))
                .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones[0].tipo_pago')].mensaje")
                        .value("El tipo de pago debe ser Efectivo, Debito o Credito"))
                .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones[0].monto')].mensaje")
                        .value("El monto debe ser mayor que cero"));
    }

    @Test
    void postTransaccionesRejectsLowercaseEnumValues() throws Exception {
        String request = """
                {
                  "transacciones": [
                    {
                      "tipo": "egreso",
                      "fecha": "2026-07-20",
                      "descripcion": "Taxi",
                      "tipo_pago": "debito",
                      "monto": 75
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/transacciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones[0].tipo')].mensaje")
                        .value("El tipo de transacción debe ser Ingreso o Egreso"))
                .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones[0].tipo_pago')].mensaje")
                        .value("El tipo de pago debe ser Efectivo, Debito o Credito"));
    }

    @Test
    void postTransaccionesRejectsStringAmount() throws Exception {
        String request = """
                {
                  "transacciones": [
                    {
                      "tipo": "Egreso",
                      "fecha": "2026-07-20",
                      "descripcion": "Taxi",
                      "tipo_pago": "Debito",
                      "monto": "75"
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/transacciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value("request_invalido"))
                .andExpect(jsonPath("$.errores[0].campo").value("body"))
                .andExpect(jsonPath("$.errores[0].mensaje").value("El cuerpo de la solicitud debe ser un JSON válido"));
    }

    @Test
    void postTransaccionesRejectsNullTransactionElement() throws Exception {
        mockMvc.perform(post("/transacciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"transacciones\": [null] }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value("request_invalido"))
                .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones[0]')].mensaje")
                        .value("La transacción no puede ser nula"));
    }

    @Test
    void postTransaccionesRejectsStringMesesADeber() throws Exception {
        String request = """
                {
                  "transacciones": [
                    {
                      "tipo": "Egreso",
                      "fecha": "2026-07-20",
                      "descripcion": "Taxi",
                      "tipo_pago": "Credito",
                      "meses_a_deber": "3",
                      "monto": 75
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/transacciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value("request_invalido"))
                .andExpect(jsonPath("$.errores[0].campo").value("body"))
                .andExpect(jsonPath("$.errores[0].mensaje").value("El cuerpo de la solicitud debe ser un JSON válido"));
    }

    @Test
    void postTransaccionesAcceptsMoreThanOneHundredTransactions() throws Exception {
        String transaction = """
                    {
                      "tipo": "Egreso",
                      "fecha": "2026-07-20",
                      "descripcion": "Taxi",
                      "tipo_pago": "Debito",
                      "monto": 75
                    }
                """;
        String request = "{ \"transacciones\": [" + String.join(",", Collections.nCopies(101, transaction)) + "] }";

        mockMvc.perform(post("/transacciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transacciones_clasificadas", hasSize(101)));
    }

    @Test
    void postTransaccionesRejectsMissingTipoPagoOnlyForEgreso() throws Exception {
        String request = """
                {
                  "transacciones": [
                    {
                      "tipo": "Egreso",
                      "fecha": "2026-07-20",
                      "descripcion": "Supermercado",
                      "monto": 120
                    },
                    {
                      "tipo": "Ingreso",
                      "fecha": "2026-07-21",
                      "descripcion": "Salary",
                      "monto": 3000
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/transacciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones[0].tipo_pago')].mensaje")
                        .value("El tipo de pago es obligatorio cuando el tipo de transacción es Egreso"))
                .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones[1].tipo_pago')]").doesNotExist());
    }

    @Test
    void postTransaccionesRejectsCreditoWithoutMesesADeber() throws Exception {
        String request = """
                {
                  "transacciones": [
                    {
                      "tipo": "Egreso",
                      "fecha": "2026-07-20",
                      "descripcion": "Taxi",
                      "tipo_pago": "Credito",
                      "monto": 75
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/transacciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones[0].meses_a_deber')].mensaje")
                        .value("Los meses a deber son obligatorios cuando el tipo de pago es Credito"));
    }

    @Test
    void postTransaccionesRejectsMalformedJsonBody() throws Exception {
        mockMvc.perform(post("/transacciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value("request_invalido"))
                .andExpect(jsonPath("$.errores[0].campo").value("body"))
                .andExpect(jsonPath("$.errores[0].mensaje").value("El cuerpo de la solicitud debe ser un JSON válido"));
    }

    private static String validRequest() {
        return """
                {
                  "transacciones": [
                    {
                      "tipo": "Egreso",
                      "fecha": "2026-07-20",
                      "descripcion": "Supermercado",
                      "tipo_pago": "Debito",
                      "monto": 200
                    },
                    {
                      "tipo": "Egreso",
                      "fecha": "2026-07-21",
                      "descripcion": "Taxi",
                      "tipo_pago": "Credito",
                      "meses_a_deber": 3,
                      "monto": 450
                    },
                    {
                      "tipo": "Ingreso",
                      "fecha": "2026-07-01",
                      "descripcion": "Salary",
                      "monto": 3000
                    }
                  ]
                }
                """;
    }
}
