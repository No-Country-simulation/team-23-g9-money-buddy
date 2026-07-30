package com.moneybuddy.analysis.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Schema(description = "Respuesta de análisis financiero generada por el backend.")
public record AnalisisFinancieroResponse(
    @Schema(description = "Indica si la solicitud se procesó correctamente.", example = "true")
    @JsonProperty("success") boolean success,
    @Schema(description = "Mensaje de resultado legible.", example = "Análisis financiero generado exitosamente")
    @JsonProperty("message") String message,
    @Schema(description = "Datos calculados del análisis.")
    @JsonProperty("data") Data data) {

    @Schema(description = "Perfil financiero, indicadores, transacciones clasificadas y recomendaciones calculadas.")
    public record Data(
        @Schema(description = "Etiqueta del perfil calculada por el backend.", example = "requiere_atencion")
        @JsonProperty("perfil_financiero") String perfilFinanciero,
        @Schema(description = "Puntaje financiero calculado por el backend.", example = "72")
        @JsonProperty("score_financiero") int scoreFinanciero,
        @Schema(description = "Resumen de gastos por categoría calculado por el backend.", example = "{\"alimentos\":350,\"tecnologia\":1200}")
        @JsonProperty("resumen_gastos") Map<String, BigDecimal> resumenGastos,
        @Schema(description = "Indicadores financieros calculados por el backend.")
        @JsonProperty("indicadores") Indicadores indicadores,
        @ArraySchema(schema = @Schema(implementation = TransaccionClasificada.class))
        @JsonProperty("transacciones_clasificadas") List<TransaccionClasificada> transaccionesClasificadas,
        @ArraySchema(schema = @Schema(description = "Recomendación generada por el backend.", example = "Mantén protegido tu hábito de ahorro actual."))
        @JsonProperty("recomendaciones") List<String> recomendaciones) {
    }

    @Schema(description = "Indicadores financieros calculados por el backend a partir de los datos de la solicitud.")
    public record Indicadores(
        @Schema(description = "Ingreso mensual copiado de la solicitud.", example = "4500")
        @JsonProperty("ingreso_mensual") BigDecimal ingresoMensual,
        @Schema(description = "Deuda total calculada por el backend solo a partir de transacciones de crédito.", example = "1200")
        @JsonProperty("deuda_total") BigDecimal deudaTotal,
        @Schema(description = "Crédito total copiado de la solicitud.", example = "15000")
        @JsonProperty("credito_total") BigDecimal creditoTotal,
        @Schema(description = "Nivel de endeudamiento calculado por el backend como porcentaje de credito_total.", example = "8.00")
        @JsonProperty("nivel_endeudamiento") BigDecimal nivelEndeudamiento,
        @Schema(description = "Frecuencia de ahorro copiada de la solicitud.", example = "MEDIA")
        @JsonProperty("frecuencia_ahorro") String frecuenciaAhorro,
        @Schema(description = "Pago mensual de deudas copiado de la solicitud.", example = "600")
        @JsonProperty("pago_mensual_deudas") BigDecimal pagoMensualDeudas,
        @Schema(description = "Gasto total calculado por el backend.", example = "1550")
        @JsonProperty("gasto_total") BigDecimal gastoTotal,
        @Schema(description = "Ratio mensual de pago de deudas calculado por el backend.", example = "0.1333")
        @JsonProperty("ratio_pago_deudas") BigDecimal ratioPagoDeudas,
        @Schema(description = "Ratio deuda-ingreso calculado por el backend.", example = "0.2667")
        @JsonProperty("ratio_deuda_ingreso") BigDecimal ratioDeudaIngreso,
        @Schema(description = "Distribución porcentual por categoría de gasto calculada por el backend.", example = "{\"alimentos\":22.58,\"tecnologia\":77.42}")
        @JsonProperty("porcentaje_categorias") Map<String, BigDecimal> porcentajeCategorias) {
    }

    @Schema(description = "Transacción enriquecida con una categoría calculada por el backend.")
    public record TransaccionClasificada(
        @Schema(description = "Tipo de transacción.", example = "Egreso")
        @JsonProperty("tipo") String tipo,
        @Schema(description = "Fecha de la transacción.", example = "2026-07-05")
        @JsonProperty("fecha") LocalDate fecha,
        @Schema(description = "Descripción de la transacción.", example = "Supermercado")
        @JsonProperty("descripcion") String descripcion,
        @Schema(description = "Tipo de pago para transacciones de egreso.", example = "Debito")
        @JsonProperty("tipo_pago") String tipoPago,
        @Schema(description = "Meses a deber cuando el tipo de pago es crédito.", example = "12")
        @JsonProperty("meses_a_deber") Integer mesesADeber,
        @Schema(description = "Monto de la transacción.", example = "350")
        @JsonProperty("monto") BigDecimal monto,
        @Schema(description = "Categoría de la transacción calculada por el backend.", example = "alimentos")
        @JsonProperty("categoria") String categoria) {
    }
}
