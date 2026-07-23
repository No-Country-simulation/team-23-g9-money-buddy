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

  @Test
  void postAnalisisFinancieroRejectsMissingRequiredFinancialFields() throws Exception {
    String request = """
        {
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
            }
          ]
        }
        """;

    mockMvc.perform(post("/analisis-financiero")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.estado").value("request_invalido"))
        .andExpect(jsonPath("$.errores").isArray())
        .andExpect(jsonPath("$.errores[?(@.campo == 'ingreso_mensual')]").exists());
  }

  @Test
  void postAnalisisFinancieroRejectsInvalidTransactionFields() throws Exception {
    String request = """
        {
          "ingreso_mensual": 1000,
          "ahorro_mensual": 200,
          "deuda_total": 1500,
          "pago_mensual_deudas": 150,
          "transacciones": [
            {
              "descripcion": "",
              "categoria": "comida",
              "monto": -120.50,
              "fecha": null,
              "tipo": "gastos"
            }
          ]
        }
        """;

    mockMvc.perform(post("/analisis-financiero")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.estado").value("request_invalido"))
        .andExpect(jsonPath("$.errores").isArray())
        .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones[0].descripcion')]").exists())
        .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones[0].monto')]").exists())
        .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones[0].fecha')]").exists());
  }

  @Test
  void postAnalisisFinancieroRejectsMalformedJsonBody() throws Exception {
    String request = """
        {
          "ingreso_mensual": ,
          "ahorro_mensual": 200,
          "deuda_total": 1500,
          "pago_mensual_deudas": 150,
          "transacciones": []
        }
        """;

    mockMvc.perform(post("/analisis-financiero")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.estado").value("request_invalido"))
        .andExpect(jsonPath("$.errores").isArray())
        .andExpect(jsonPath("$.errores[0].campo").value("body"))
        .andExpect(jsonPath("$.errores[0].mensaje").value("El cuerpo de la solicitud debe ser un JSON válido"));
  }

  @Test
  void postAnalisisFinancieroConDatosValidosDevuelveEstadoAnalisisGenerado() throws Exception {
    String request = """
        {
          "ingreso_mensual": 3000,
          "ahorro_mensual": 500,
          "deuda_total": 0,
          "pago_mensual_deudas": 0,
          "transacciones": [
            {
              "descripcion": "Pago de alquiler",
              "categoria": "vivienda",
              "monto": 800,
              "fecha": "2026-07-15",
              "tipo": "gastos"
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
        .andExpect(jsonPath("$.resumen").exists())
        .andExpect(jsonPath("$.resumen.salud_financiera").isString())
        .andExpect(jsonPath("$.resumen.flujo_mensual_estimado").isNumber())
        .andExpect(jsonPath("$.resumen.total_transacciones").value(1))
        .andExpect(jsonPath("$.indicadores").exists())
        .andExpect(jsonPath("$.indicadores.tasa_ahorro").isNumber())
        .andExpect(jsonPath("$.indicadores.ratio_pago_deudas").isNumber())
        .andExpect(jsonPath("$.indicadores.ratio_deuda_ingreso").isNumber())
        .andExpect(jsonPath("$.indicadores.nivel_ahorro").isString())
        .andExpect(jsonPath("$.indicadores.nivel_deuda").isString())
        .andExpect(jsonPath("$.recomendaciones").isArray())
        .andExpect(jsonPath("$.recomendaciones").isNotEmpty());
  }

  @Test
  void postAnalisisFinancieroRechazaCuandoFaltanMultiplesCamposFinancieros() throws Exception {
    String request = """
        {
          "ahorro_mensual": 200,
          "pago_mensual_deudas": 150,
          "transacciones": [
            {
              "descripcion": "Supermercado",
              "categoria": "alimentacion",
              "monto": 120.50,
              "fecha": "2026-07-20",
              "tipo": "gastos"
            }
          ]
        }
        """;

    mockMvc.perform(post("/analisis-financiero")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.estado").value("request_invalido"))
        .andExpect(jsonPath("$.errores").isArray())
        .andExpect(jsonPath("$.errores[?(@.campo == 'ingreso_mensual')]").exists())
        .andExpect(jsonPath("$.errores[?(@.campo == 'deuda_total')]").exists());
  }

  @Test
  void postAnalisisFinancieroRechazaIngresoMensualCero() throws Exception {
    String request = """
        {
          "ingreso_mensual": 0,
          "ahorro_mensual": 200,
          "deuda_total": 1500,
          "pago_mensual_deudas": 150,
          "transacciones": [
            {
              "descripcion": "Supermercado",
              "categoria": "alimentacion",
              "monto": 120.50,
              "fecha": "2026-07-20",
              "tipo": "gastos"
            }
          ]
        }
        """;

    mockMvc.perform(post("/analisis-financiero")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.estado").value("request_invalido"))
        .andExpect(jsonPath("$.errores").isArray())
        .andExpect(jsonPath("$.errores[?(@.campo == 'ingreso_mensual')]").exists())
        .andExpect(jsonPath("$.errores[?(@.campo == 'ingreso_mensual')].mensaje")
            .value("El ingreso mensual debe ser mayor que cero"));
  }

  @Test
  void postAnalisisFinancieroRechazaTransaccionSinCamposObligatorios() throws Exception {
    String request = """
        {
          "ingreso_mensual": 1000,
          "ahorro_mensual": 200,
          "deuda_total": 1500,
          "pago_mensual_deudas": 150,
          "transacciones": [
            {
              "monto": 120.50
            }
          ]
        }
        """;

    mockMvc.perform(post("/analisis-financiero")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.estado").value("request_invalido"))
        .andExpect(jsonPath("$.errores").isArray())
        .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones[0].descripcion')]").exists())
        .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones[0].categoria')]").exists())
        .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones[0].fecha')]").exists())
        .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones[0].tipo')]").exists());
  }

  @Test
  void postAnalisisFinancieroRechazaListaDeTransaccionesVacia() throws Exception {
    String request = """
        {
          "ingreso_mensual": 1000,
          "ahorro_mensual": 200,
          "deuda_total": 1500,
          "pago_mensual_deudas": 150,
          "transacciones": []
        }
        """;

    mockMvc.perform(post("/analisis-financiero")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.estado").value("request_invalido"))
        .andExpect(jsonPath("$.errores").isArray())
        .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones')]").exists())
        .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones')].mensaje")
            .value("La lista de transacciones es obligatoria y no puede estar vacía"));
  }

  @Test
  void postAnalisisFinancieroRechazaSinCuerpoDeRequest() throws Exception {
    mockMvc.perform(post("/analisis-financiero")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.estado").value("request_invalido"))
        .andExpect(jsonPath("$.errores").isArray())
        .andExpect(jsonPath("$.errores[0].campo").value("body"))
        .andExpect(jsonPath("$.errores[0].mensaje").value("El cuerpo de la solicitud debe ser un JSON válido"));
  }
}
