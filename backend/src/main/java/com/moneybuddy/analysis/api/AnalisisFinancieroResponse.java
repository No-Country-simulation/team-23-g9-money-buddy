package com.moneybuddy.analysis.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record AnalisisFinancieroResponse(
    @JsonProperty("success") boolean success,
    @JsonProperty("message") String message,
    @JsonProperty("data") Data data) {

    public record Data(
        @JsonProperty("perfil_financiero") String perfilFinanciero,
        @JsonProperty("score_financiero") int scoreFinanciero,
        @JsonProperty("resumen_gastos") Map<String, BigDecimal> resumenGastos,
        @JsonProperty("indicadores") Indicadores indicadores,
        @JsonProperty("transacciones_clasificadas") List<TransaccionClasificada> transaccionesClasificadas,
        @JsonProperty("recomendaciones") List<String> recomendaciones) {
    }

    public record Indicadores(
        @JsonProperty("ingreso_mensual") BigDecimal ingresoMensual,
        @JsonProperty("deuda_total") BigDecimal deudaTotal,
        @JsonProperty("credito_total") BigDecimal creditoTotal,
        @JsonProperty("nivel_endeudamiento") BigDecimal nivelEndeudamiento,
        @JsonProperty("frecuencia_ahorro") String frecuenciaAhorro,
        @JsonProperty("pago_mensual_deudas") BigDecimal pagoMensualDeudas,
        @JsonProperty("gasto_total") BigDecimal gastoTotal,
        @JsonProperty("ratio_pago_deudas") BigDecimal ratioPagoDeudas,
        @JsonProperty("ratio_deuda_ingreso") BigDecimal ratioDeudaIngreso,
        @JsonProperty("porcentaje_categorias") Map<String, BigDecimal> porcentajeCategorias) {
    }

    public record TransaccionClasificada(
        @JsonProperty("tipo") String tipo,
        @JsonProperty("fecha") LocalDate fecha,
        @JsonProperty("descripcion") String descripcion,
        @JsonProperty("tipo_pago") String tipoPago,
        @JsonProperty("meses_a_deber") Integer mesesADeber,
        @JsonProperty("monto") BigDecimal monto,
        @JsonProperty("categoria") String categoria) {
    }
}
