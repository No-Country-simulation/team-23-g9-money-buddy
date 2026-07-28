package com.moneybuddy.userdata.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DatosUsuarioController.class)
class DatosUsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void postDatosUsuarioReturnsValidatedFinancialData() throws Exception {
        mockMvc.perform(post("/datos-usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Datos financieros validados correctamente"))
                .andExpect(jsonPath("$.data.ingreso_mensual").value(4500))
                .andExpect(jsonPath("$.data.credito_total").value(20000))
                .andExpect(jsonPath("$.data.frecuencia_ahorro").value("MEDIA"))
                .andExpect(jsonPath("$.data.id").doesNotExist())
                .andExpect(jsonPath("$.data.created_at").doesNotExist())
                .andExpect(jsonPath("$.data.updated_at").doesNotExist());
    }

    @Test
    void putDatosUsuarioReturnsValidatedFinancialData() throws Exception {
        mockMvc.perform(put("/datos-usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Datos financieros validados correctamente"))
                .andExpect(jsonPath("$.data.ingreso_mensual").value(4500))
                .andExpect(jsonPath("$.data.credito_total").value(20000))
                .andExpect(jsonPath("$.data.frecuencia_ahorro").value("MEDIA"));
    }

    @Test
    void postDatosUsuarioRejectsMissingRequiredFields() throws Exception {
        String request = """
                {}
                """;

        mockMvc.perform(post("/datos-usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value("request_invalido"))
                .andExpect(jsonPath("$.errores[?(@.campo == 'ingreso_mensual')].mensaje")
                        .value("El ingreso mensual es obligatorio"))
                .andExpect(jsonPath("$.errores[?(@.campo == 'credito_total')].mensaje")
                        .value("El crédito total es obligatorio"))
                .andExpect(jsonPath("$.errores[?(@.campo == 'frecuencia_ahorro')].mensaje")
                        .value("La frecuencia de ahorro es obligatoria"));
    }

    @Test
    void postDatosUsuarioRejectsInvalidNumericValues() throws Exception {
        String request = """
                {
                  "ingreso_mensual": 0,
                  "credito_total": -1,
                  "frecuencia_ahorro": "MEDIA"
                }
                """;

        mockMvc.perform(post("/datos-usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value("request_invalido"))
                .andExpect(jsonPath("$.errores[?(@.campo == 'ingreso_mensual')].mensaje")
                        .value("El ingreso mensual debe ser mayor que cero"))
                .andExpect(jsonPath("$.errores[?(@.campo == 'credito_total')].mensaje")
                        .value("El crédito total debe ser mayor o igual que cero"));
    }

    @Test
    void postDatosUsuarioRejectsInvalidFrecuenciaAhorro() throws Exception {
        String request = """
                {
                  "ingreso_mensual": 4500,
                  "credito_total": 20000,
                  "frecuencia_ahorro": "SEMANAL"
                }
                """;

        mockMvc.perform(post("/datos-usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value("request_invalido"))
                .andExpect(jsonPath("$.errores[?(@.campo == 'frecuencia_ahorro')].mensaje")
                        .value("La frecuencia de ahorro debe ser NULA, BAJA, MEDIA o ALTA"));
    }

    @Test
    void postDatosUsuarioAcceptsLowercaseFrecuenciaAhorro() throws Exception {
        String request = """
                {
                  "ingreso_mensual": 4500,
                  "credito_total": 20000,
                  "frecuencia_ahorro": "media"
                }
                """;

        mockMvc.perform(post("/datos-usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.frecuencia_ahorro").value("media"));
    }

    @Test
    void postDatosUsuarioIsStatelessAcrossRequests() throws Exception {
        String firstRequest = """
                {
                  "ingreso_mensual": 4500,
                  "credito_total": 20000,
                  "frecuencia_ahorro": "MEDIA"
                }
                """;
        String secondRequest = """
                {
                  "ingreso_mensual": 1200,
                  "credito_total": 0,
                  "frecuencia_ahorro": "BAJA"
                }
                """;

        mockMvc.perform(post("/datos-usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(firstRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ingreso_mensual").value(4500))
                .andExpect(jsonPath("$.data.credito_total").value(20000))
                .andExpect(jsonPath("$.data.frecuencia_ahorro").value("MEDIA"));

        mockMvc.perform(post("/datos-usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(secondRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ingreso_mensual").value(1200))
                .andExpect(jsonPath("$.data.credito_total").value(0))
                .andExpect(jsonPath("$.data.frecuencia_ahorro").value("BAJA"));
    }

    @Test
    void postDatosUsuarioRejectsMalformedJsonBody() throws Exception {
        mockMvc.perform(post("/datos-usuario")
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
                  "ingreso_mensual": 4500,
                  "credito_total": 20000,
                  "frecuencia_ahorro": "MEDIA"
                }
                """;
    }
}
