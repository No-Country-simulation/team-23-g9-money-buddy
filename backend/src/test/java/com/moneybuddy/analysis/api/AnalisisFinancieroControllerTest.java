package com.moneybuddy.analysis.api;

import com.moneybuddy.analysis.application.AnalisisFinancieroService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalisisFinancieroController.class)
@Import(AnalisisFinancieroService.class)
class AnalisisFinancieroControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void postAnalisisFinancieroReturnsStableJsonResponse() throws Exception {
    String request = """
        {
          "ingreso_mensual": 1000,
          "ahorro_mensual": 200,
          "deuda_total": 1500,
          "pago_mensual_deudas": 150,
          "transacciones": [
            {
              "descripcion": "Tienda de comestibles",
              "categoria": "comida",
              "monto": 120.50,
              "fecha": "2026-07-20",
              "tipo": "gastos"
            },
            {
              "descripcion": "Salario",
              "categoria": "ingreso",
              "monto": 1000,
              "fecha": "2026-07-01",
              "tipo": "ingreso"
            }
          ]
        }
        """;

    mockMvc.perform(post("/analisis-financiero")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.estado").value("analisis_generado"))
        .andExpect(jsonPath("$.resumen.salud_financiera").value("estable"))
        .andExpect(jsonPath("$.resumen.flujo_mensual_estimado").value(650))
        .andExpect(jsonPath("$.resumen.total_transacciones").value(2))
        .andExpect(jsonPath("$.indicadores.tasa_ahorro").value(0.2000))
        .andExpect(jsonPath("$.indicadores.ratio_pago_deudas").value(0.1500))
        .andExpect(jsonPath("$.indicadores.ratio_deuda_ingreso").value(1.5000))
        .andExpect(jsonPath("$.indicadores.nivel_ahorro").value("saludable"))
        .andExpect(jsonPath("$.indicadores.nivel_deuda").value("controlada"))
        .andExpect(jsonPath("$.recomendaciones").isArray())
        .andExpect(jsonPath("$.recomendaciones[0]").value("Mantén protegido el hábito de ahorro actual."))
        .andExpect(jsonPath("$.recomendaciones[1]").value("La presión de deuda está controlada; evita asumir nuevas obligaciones recurrentes."));
  }
}
