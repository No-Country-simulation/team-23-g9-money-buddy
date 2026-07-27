package com.moneybuddy.analysis.api;

import com.moneybuddy.analysis.application.AnalisisFinancieroService;
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

@WebMvcTest(AnalisisFinancieroController.class)
@Import(AnalisisFinancieroService.class)
class AnalisisFinancieroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void postAnalisisFinancieroReturnsIssue27ResponseShape() throws Exception {
        mockMvc.perform(post("/analisis-financiero")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Análisis financiero generado exitosamente"))
                .andExpect(jsonPath("$.data.perfil_financiero").value("estable"))
                .andExpect(jsonPath("$.data.score_financiero").value(85))
                .andExpect(jsonPath("$.data.indicadores.ingreso_mensual").value(3000))
                .andExpect(jsonPath("$.data.indicadores.credito_total").value(2000))
                .andExpect(jsonPath("$.data.indicadores.deuda_total").value(450))
                .andExpect(jsonPath("$.data.indicadores.nivel_endeudamiento").value(22.50))
                .andExpect(jsonPath("$.data.indicadores.frecuencia_ahorro").value("ALTA"))
                .andExpect(jsonPath("$.data.indicadores.pago_mensual_deudas").value(300))
                .andExpect(jsonPath("$.data.indicadores.ratio_pago_deudas").value(0.1000))
                .andExpect(jsonPath("$.data.indicadores.ratio_deuda_ingreso").value(0.1500))
                .andExpect(jsonPath("$.data.indicadores.gasto_total").value(650))
                .andExpect(jsonPath("$.data.resumen_gastos.alimentos").value(200))
                .andExpect(jsonPath("$.data.resumen_gastos.transporte").value(450))
                .andExpect(jsonPath("$.data.indicadores.porcentaje_categorias.alimentos").value(30.77))
                .andExpect(jsonPath("$.data.indicadores.porcentaje_categorias.transporte").value(69.23))
                .andExpect(jsonPath("$.data.transacciones_clasificadas", hasSize(3)))
                .andExpect(jsonPath("$.data.transacciones_clasificadas[0].categoria").value("alimentos"))
                .andExpect(jsonPath("$.data.transacciones_clasificadas[1].categoria").value("transporte"))
                .andExpect(jsonPath("$.data.transacciones_clasificadas[2].categoria").value("ingreso"))
                .andExpect(jsonPath("$.data.recomendaciones").isArray());
    }

    @Test
    void postAnalisisFinancieroAcceptsSnakeCaseTipoPago() throws Exception {
        String request = """
                {
                  "ingreso_mensual": 3000,
                  "credito_total": 1000,
                  "frecuencia_ahorro": "MEDIA",
                  "pago_mensual_deudas": 100,
                  "transacciones": [
                    {
                      "tipo": "Egreso",
                      "fecha": "2026-07-20",
                      "descripcion": "Taxi",
                      "tipo_pago": "Debito",
                      "monto": 75
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/analisis-financiero")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transacciones_clasificadas[0].tipo_pago").value("Debito"));
    }

    @Test
    void postAnalisisFinancieroKeepsDebtSafeWhenCreditTotalIsZero() throws Exception {
        String request = """
                {
                  "ingreso_mensual": 3000,
                  "credito_total": 0,
                  "frecuencia_ahorro": "ALTA",
                  "pago_mensual_deudas": 0,
                  "transacciones": [
                    {
                      "tipo": "Egreso",
                      "fecha": "2026-07-20",
                      "descripcion": "Taxi",
                      "tipo_pago": "Credito",
                      "meses_a_deber": 1,
                      "monto": 75
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/analisis-financiero")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.indicadores.deuda_total").value(75))
                .andExpect(jsonPath("$.data.indicadores.nivel_endeudamiento").value(0.00));
    }

    @Test
    void postAnalisisFinancieroRejectsMissingTipoPagoOnlyForEgreso() throws Exception {
        String request = """
                {
                  "ingreso_mensual": 3000,
                  "credito_total": 1000,
                  "frecuencia_ahorro": "MEDIA",
                  "pago_mensual_deudas": 100,
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

        mockMvc.perform(post("/analisis-financiero")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value("request_invalido"))
                .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones[0].tipo_pago')].mensaje")
                        .value("El tipo de pago es obligatorio cuando el tipo de transacción es Egreso"))
                .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones[1].tipo_pago')]").doesNotExist());
    }

    @Test
    void postAnalisisFinancieroRejectsCreditoWithoutMesesADeber() throws Exception {
        String request = """
                {
                  "ingreso_mensual": 3000,
                  "credito_total": 1000,
                  "frecuencia_ahorro": "MEDIA",
                  "pago_mensual_deudas": 100,
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

        mockMvc.perform(post("/analisis-financiero")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones[0].meses_a_deber')].mensaje")
                        .value("Los meses a deber son obligatorios cuando el tipo de pago es Credito"));
    }

    @Test
    void postAnalisisFinancieroRejectsInvalidTipoPagoForEgreso() throws Exception {
        String request = """
                {
                  "ingreso_mensual": 3000,
                  "credito_total": 1000,
                  "frecuencia_ahorro": "MEDIA",
                  "pago_mensual_deudas": 100,
                  "transacciones": [
                    {
                      "tipo": "Egreso",
                      "fecha": "2026-07-20",
                      "descripcion": "Taxi",
                      "tipo_pago": "Tarjeta",
                      "monto": 75
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/analisis-financiero")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones[0].tipo_pago')].mensaje")
                        .value("El tipo de pago debe ser Efectivo, Debito o Credito"));
    }

    @Test
    void postAnalisisFinancieroRejectsInvalidTipoPagoForIngresoWhenPresent() throws Exception {
        String request = """
                {
                  "ingreso_mensual": 3000,
                  "credito_total": 1000,
                  "frecuencia_ahorro": "MEDIA",
                  "pago_mensual_deudas": 100,
                  "transacciones": [
                    {
                      "tipo": "Ingreso",
                      "fecha": "2026-07-20",
                      "descripcion": "Salary",
                      "tipo_pago": "Transferencia",
                      "monto": 3000
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/analisis-financiero")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones[0].tipo_pago')].mensaje")
                        .value("El tipo de pago debe ser Efectivo, Debito o Credito"));
    }

    @Test
    void postAnalisisFinancieroRejectsBlankTipoPagoForIngresoWhenPresent() throws Exception {
        String request = """
                {
                  "ingreso_mensual": 3000,
                  "credito_total": 1000,
                  "frecuencia_ahorro": "MEDIA",
                  "pago_mensual_deudas": 100,
                  "transacciones": [
                    {
                      "tipo": "Ingreso",
                      "fecha": "2026-07-20",
                      "descripcion": "Salary",
                      "tipo_pago": "   ",
                      "monto": 3000
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/analisis-financiero")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones[0].tipo_pago')].mensaje")
                        .value("El tipo de pago debe ser Efectivo, Debito o Credito"));
    }

    @Test
    void postAnalisisFinancieroUsesFinalOfficialCategoryKeys() throws Exception {
        String request = """
                {
                  "ingreso_mensual": 3000,
                  "credito_total": 1000,
                  "frecuencia_ahorro": "MEDIA",
                  "pago_mensual_deudas": 100,
                  "transacciones": [
                    { "tipo": "Egreso", "fecha": "2026-07-20", "descripcion": "Restaurante", "tipo_pago": "Efectivo", "monto": 10 },
                    { "tipo": "Egreso", "fecha": "2026-07-20", "descripcion": "Metro", "tipo_pago": "Debito", "monto": 10 },
                    { "tipo": "Egreso", "fecha": "2026-07-20", "descripcion": "Cine", "tipo_pago": "Efectivo", "monto": 10 },
                    { "tipo": "Egreso", "fecha": "2026-07-20", "descripcion": "Farmacia", "tipo_pago": "Efectivo", "monto": 10 },
                    { "tipo": "Egreso", "fecha": "2026-07-20", "descripcion": "Alquiler", "tipo_pago": "Debito", "monto": 10 },
                    { "tipo": "Egreso", "fecha": "2026-07-20", "descripcion": "Curso", "tipo_pago": "Efectivo", "monto": 10 },
                    { "tipo": "Egreso", "fecha": "2026-07-20", "descripcion": "Internet", "tipo_pago": "Efectivo", "monto": 10 },
                    { "tipo": "Egreso", "fecha": "2026-07-20", "descripcion": "Ropa", "tipo_pago": "Debito", "monto": 10 },
                    { "tipo": "Egreso", "fecha": "2026-07-20", "descripcion": "Laptop", "tipo_pago": "Debito", "monto": 10 },
                    { "tipo": "Egreso", "fecha": "2026-07-20", "descripcion": "Hotel", "tipo_pago": "Efectivo", "monto": 10 },
                    { "tipo": "Ingreso", "fecha": "2026-07-20", "descripcion": "Salario", "monto": 1000 }
                  ]
                }
                """;

        mockMvc.perform(post("/analisis-financiero")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resumen_gastos.alimentos").value(10))
                .andExpect(jsonPath("$.data.resumen_gastos.transporte").value(10))
                .andExpect(jsonPath("$.data.resumen_gastos.salud").value(10))
                .andExpect(jsonPath("$.data.resumen_gastos.vivienda").value(10))
                .andExpect(jsonPath("$.data.resumen_gastos.educacion").value(10))
                .andExpect(jsonPath("$.data.resumen_gastos.ocio_entretenimiento").value(10))
                .andExpect(jsonPath("$.data.resumen_gastos.servicios").value(10))
                .andExpect(jsonPath("$.data.resumen_gastos.ropa_calzado").value(10))
                .andExpect(jsonPath("$.data.resumen_gastos.tecnologia").value(10))
                .andExpect(jsonPath("$.data.resumen_gastos.otros").value(10))
                .andExpect(jsonPath("$.data.resumen_gastos.viajes").doesNotExist())
                .andExpect(jsonPath("$.data.resumen_gastos.compras").doesNotExist())
                .andExpect(jsonPath("$.data.resumen_gastos.ingreso").doesNotExist())
                .andExpect(jsonPath("$.data.resumen_gastos.entretenimiento").doesNotExist())
                .andExpect(jsonPath("$.data.indicadores.porcentaje_categorias.ocio_entretenimiento").value(10.00))
                .andExpect(jsonPath("$.data.indicadores.porcentaje_categorias.ropa_calzado").value(10.00))
                .andExpect(jsonPath("$.data.indicadores.porcentaje_categorias.tecnologia").value(10.00))
                .andExpect(jsonPath("$.data.indicadores.porcentaje_categorias.otros").value(10.00))
                .andExpect(jsonPath("$.data.indicadores.porcentaje_categorias.viajes").doesNotExist())
                .andExpect(jsonPath("$.data.indicadores.porcentaje_categorias.compras").doesNotExist())
                .andExpect(jsonPath("$.data.indicadores.porcentaje_categorias.ingreso").doesNotExist())
                .andExpect(jsonPath("$.data.transacciones_clasificadas[2].categoria").value("ocio_entretenimiento"))
                .andExpect(jsonPath("$.data.transacciones_clasificadas[7].categoria").value("ropa_calzado"))
                .andExpect(jsonPath("$.data.transacciones_clasificadas[8].categoria").value("tecnologia"))
                .andExpect(jsonPath("$.data.transacciones_clasificadas[9].categoria").value("otros"))
                .andExpect(jsonPath("$.data.transacciones_clasificadas[10].categoria").value("ingreso"));
    }

    @Test
    void postAnalisisFinancieroRejectsInvalidSavingsFrequency() throws Exception {
        String request = validRequest().replace("\"ALTA\"", "\"MENSUAL\"");

        mockMvc.perform(post("/analisis-financiero")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[?(@.campo == 'frecuencia_ahorro')].mensaje")
                        .value("La frecuencia de ahorro debe ser NULA, BAJA, MEDIA o ALTA"));
    }

    @Test
    void postAnalisisFinancieroRejectsPublicDebtTotalInsteadOfNewRequiredFields() throws Exception {
        String request = """
                {
                  "ingreso_mensual": 3000,
                  "deuda_total": 1000,
                  "nivel_endeudamiento": 50,
                  "pago_mensual_deudas": 100,
                  "transacciones": [
                    {
                      "tipo": "Ingreso",
                      "fecha": "2026-07-21",
                      "descripcion": "Salary",
                      "monto": 3000
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/analisis-financiero")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[?(@.campo == 'credito_total')].mensaje")
                        .value("El crédito total es obligatorio"))
                .andExpect(jsonPath("$.errores[?(@.campo == 'frecuencia_ahorro')].mensaje")
                        .value("La frecuencia de ahorro es obligatoria"))
                .andExpect(jsonPath("$.errores[?(@.campo == 'deuda_total')]").doesNotExist())
                .andExpect(jsonPath("$.errores[?(@.campo == 'nivel_endeudamiento')]").doesNotExist());
    }

    @Test
    void postAnalisisFinancieroRejectsTooManyTransactions() throws Exception {
        StringBuilder transactions = new StringBuilder();
        for (int index = 0; index < 101; index++) {
            if (!transactions.isEmpty()) {
                transactions.append(",");
            }
            transactions.append("""
                    {
                      "tipo": "Ingreso",
                      "fecha": "2026-07-21",
                      "descripcion": "Salary",
                      "monto": 3000
                    }
                    """);
        }

        String request = """
                {
                  "ingreso_mensual": 3000,
                  "credito_total": 1000,
                  "frecuencia_ahorro": "MEDIA",
                  "pago_mensual_deudas": 100,
                  "transacciones": [%s]
                }
                """.formatted(transactions);

        mockMvc.perform(post("/analisis-financiero")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[?(@.campo == 'transacciones')].mensaje")
                        .value("La lista de transacciones no puede contener más de 100 elementos"));
    }

    @Test
    void postAnalisisFinancieroRejectsMalformedJsonBody() throws Exception {
        mockMvc.perform(post("/analisis-financiero")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value("request_invalido"))
                .andExpect(jsonPath("$.errores[0].campo").value("body"))
                .andExpect(jsonPath("$.errores[0].mensaje").value("El cuerpo de la solicitud debe ser un JSON válido"));
    }

    private String validRequest() {
        return """
                {
                  "ingreso_mensual": 3000,
                  "credito_total": 2000,
                  "frecuencia_ahorro": "ALTA",
                  "pago_mensual_deudas": 300,
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
