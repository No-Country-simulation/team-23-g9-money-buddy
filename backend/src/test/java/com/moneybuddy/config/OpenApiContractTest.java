package com.moneybuddy.config;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void openApiDocsAreAvailable() throws Exception {
        mockMvc.perform(get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").value("API MVP de MoneyBuddy"));
    }

    @Test
    void swaggerUiHtmlIsAvailable() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/swagger-ui/index.html")));
    }

    @Test
    void openApiDocumentsFinancialAnalysisContract() throws Exception {
        mockMvc.perform(get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/analisis-financiero'].post").exists())
                .andExpect(jsonPath("$.paths['/analisis-financiero'].post.requestBody.required").value(true))
                .andExpect(jsonPath("$.paths['/analisis-financiero'].post.description", containsString("deuda_total y nivel_endeudamiento son calculados por el backend")))
                .andExpect(jsonPath("$.paths['/analisis-financiero'].post.description", containsString("tipo_pago es obligatorio solo cuando el tipo de una transacción es Egreso")))
                .andExpect(jsonPath("$.paths['/analisis-financiero'].post.description", containsString("meses_a_deber es obligatorio cuando tipo_pago es Credito")))
                .andExpect(jsonPath("$.components.schemas.AnalisisFinancieroRequest.required", hasItems("credito_total", "ingreso_mensual", "frecuencia_ahorro", "pago_mensual_deudas", "transacciones")))
                .andExpect(jsonPath("$.components.schemas.TransaccionRequest.required", hasItems("tipo", "fecha", "descripcion", "monto")))
                .andExpect(jsonPath("$.components.schemas.AnalisisFinancieroRequest.properties.credito_total").exists())
                .andExpect(jsonPath("$.components.schemas.AnalisisFinancieroRequest.properties.ingreso_mensual").exists())
                .andExpect(jsonPath("$.components.schemas.AnalisisFinancieroRequest.properties.frecuencia_ahorro.enum", hasItems("NULA", "BAJA", "MEDIA", "ALTA")))
                .andExpect(jsonPath("$.components.schemas.AnalisisFinancieroRequest.properties.pago_mensual_deudas").exists())
                .andExpect(jsonPath("$.components.schemas.TransaccionRequest.properties.tipo.enum", hasItems("Ingreso", "Egreso")))
                .andExpect(jsonPath("$.components.schemas.TransaccionRequest.properties.tipo_pago.enum", hasItems("Efectivo", "Debito", "Credito")))
                .andExpect(jsonPath("$.components.schemas.TransaccionRequest.properties.meses_a_deber.description", containsString("Obligatorio cuando tipo_pago es Credito")))
                .andExpect(jsonPath("$.components.schemas.AnalisisFinancieroResponse.properties.success").exists())
                .andExpect(jsonPath("$.components.schemas.AnalisisFinancieroResponse.properties.message").exists())
                .andExpect(jsonPath("$.components.schemas.AnalisisFinancieroResponse.properties.data").exists())
                .andExpect(jsonPath("$.paths['/analisis-financiero'].post.responses['200'].content['*/*'].examples['Análisis financiero exitoso'].value.data.transacciones_clasificadas[0].descripcion").value("Salario mensual"))
                .andExpect(jsonPath("$.paths['/analisis-financiero'].post.responses['200'].content['*/*'].examples['Análisis financiero exitoso'].value.data.transacciones_clasificadas[1].descripcion").value("Supermercado"))
                .andExpect(jsonPath("$.paths['/analisis-financiero'].post.responses['200'].content['*/*'].examples['Análisis financiero exitoso'].value.data.transacciones_clasificadas[2].descripcion").value("Computadora"))
                .andExpect(jsonPath("$.components.schemas.Data.properties.perfil_financiero.example").value("requiere_atencion"))
                .andExpect(jsonPath("$.components.schemas.Indicadores.properties.deuda_total.example").value(1200))
                .andExpect(jsonPath("$.components.schemas.Indicadores.properties.nivel_endeudamiento.example", is(8.0)))
                .andExpect(jsonPath("$.components.schemas.TransaccionClasificada.properties.categoria.example").value("alimentos"));
    }

    @Test
    void openApiDocumentsEndpointSpecificResponseExamples() throws Exception {
        mockMvc.perform(get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/datos-usuario'].post.responses['200'].content['*/*'].examples['Datos de usuario validados'].value.data.credito_total").value(15000))
                .andExpect(jsonPath("$.paths['/datos-usuario'].post.responses['200'].content['*/*'].examples['Datos de usuario validados'].value.data.frecuencia_ahorro").value("MEDIA"))
                .andExpect(jsonPath("$.paths['/datos-usuario'].put.responses['200'].content['*/*'].examples['Datos de usuario actualizados'].value.data.credito_total").value(18000))
                .andExpect(jsonPath("$.paths['/transacciones'].post.responses['200'].content['*/*'].examples['Transacciones clasificadas'].value.data.transacciones_clasificadas[0].categoria").value("ingreso"))
                .andExpect(jsonPath("$.paths['/transacciones'].post.responses['200'].content['*/*'].examples['Transacciones clasificadas'].value.data.transacciones_clasificadas[1].categoria").value("alimentos"))
                .andExpect(jsonPath("$.paths['/analisis-financiero'].post.responses['200'].content['*/*'].examples['Análisis financiero exitoso'].value.data.indicadores.deuda_total").value(1200));
    }
}
