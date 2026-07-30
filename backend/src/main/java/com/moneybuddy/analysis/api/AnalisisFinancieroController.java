package com.moneybuddy.analysis.api;

import com.moneybuddy.analysis.application.AnalisisFinancieroService;
import com.moneybuddy.api.GlobalExceptionHandler.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Análisis financiero", description = "Endpoints sin estado del MVP para validar y analizar información financiera del usuario.")
public final class AnalisisFinancieroController {

private final AnalisisFinancieroService analisisFinancieroService;

	public AnalisisFinancieroController(AnalisisFinancieroService analisisFinancieroService) {
		this.analisisFinancieroService = analisisFinancieroService;
  	}

	@PostMapping("/analisis-financiero")
    @Operation(
        summary = "Analizar un perfil financiero",
        description = "Valida el perfil financiero y las transacciones enviadas, clasifica gastos, calcula indicadores y devuelve recomendaciones. El endpoint no tiene estado y no persiste datos del usuario. Tipo_pago es obligatorio solo cuando el tipo de una transacción es Egreso. meses_a_deber es obligatorio cuando tipo_pago es Credito.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        description = "El perfil financiero que se analizará. No incluya en esta solicitud campos de respuesta calculados por el backend.",
        content = @Content(
            schema = @Schema(implementation = AnalisisFinancieroRequest.class),
            examples = @ExampleObject(
                name = "Solicitud válida de análisis financiero",
                value = """
                    {
                      "credito_total": 15000,
                      "ingreso_mensual": 4500,
                      "frecuencia_ahorro": "MEDIA",
                      "pago_mensual_deudas": 600,
                      "transacciones": [
                        {
                          "tipo": "Ingreso",
                          "fecha": "2026-07-01",
                          "descripcion": "Salario mensual",
                          "monto": 4500
                        },
                        {
                          "tipo": "Egreso",
                          "fecha": "2026-07-05",
                          "descripcion": "Supermercado",
                          "tipo_pago": "Debito",
                          "monto": 350
                        },
                        {
                          "tipo": "Egreso",
                          "fecha": "2026-07-10",
                          "descripcion": "Computadora",
                          "tipo_pago": "Credito",
                          "meses_a_deber": 12,
                          "monto": 1200
                        }
                      ]
                    }
                    """)))
    @ApiResponse(
        responseCode = "200",
        description = "Análisis financiero calculado correctamente. deuda_total, nivel_endeudamiento, gasto_total, ratios, categorías y recomendaciones son generados por el backend.",
        content = @Content(
            schema = @Schema(implementation = AnalisisFinancieroResponse.class),
            examples = @ExampleObject(
                name = "Análisis financiero exitoso",
                value = """
                    {
                      "success": true,
                      "message": "Análisis financiero generado exitosamente",
                      "data": {
                        "perfil_financiero": "requiere_atencion",
                        "score_financiero": 72,
                        "resumen_gastos": {
                          "alimentos": 350,
                          "transporte": 0,
                          "salud": 0,
                          "vivienda": 0,
                          "educacion": 0,
                          "ocio_entretenimiento": 0,
                          "servicios": 0,
                          "ropa_calzado": 0,
                          "tecnologia": 1200,
                          "otros": 0
                        },
                        "indicadores": {
                          "ingreso_mensual": 4500,
                          "deuda_total": 1200,
                          "credito_total": 15000,
                          "nivel_endeudamiento": 8.00,
                          "frecuencia_ahorro": "MEDIA",
                          "pago_mensual_deudas": 600,
                          "gasto_total": 1550,
                          "ratio_pago_deudas": 0.1333,
                          "ratio_deuda_ingreso": 0.2667,
                          "porcentaje_categorias": {
                            "alimentos": 22.58,
                            "transporte": 0.00,
                            "salud": 0.00,
                            "vivienda": 0.00,
                            "educacion": 0.00,
                            "ocio_entretenimiento": 0.00,
                            "servicios": 0.00,
                            "ropa_calzado": 0.00,
                            "tecnologia": 77.42,
                            "otros": 0.00
                          }
                        },
                        "transacciones_clasificadas": [
                          {
                            "tipo": "Ingreso",
                            "fecha": "2026-07-01",
                            "descripcion": "Salario mensual",
                            "tipo_pago": null,
                            "meses_a_deber": null,
                            "monto": 4500,
                            "categoria": "ingreso"
                          },
                          {
                            "tipo": "Egreso",
                            "fecha": "2026-07-05",
                            "descripcion": "Supermercado",
                            "tipo_pago": "Debito",
                            "meses_a_deber": null,
                            "monto": 350,
                            "categoria": "alimentos"
                          },
                          {
                            "tipo": "Egreso",
                            "fecha": "2026-07-10",
                            "descripcion": "Computadora",
                            "tipo_pago": "Credito",
                            "meses_a_deber": 12,
                            "monto": 1200,
                            "categoria": "tecnologia"
                          }
                        ],
                        "recomendaciones": [
                          "Mantén protegido tu hábito de ahorro actual.",
                          "Tu presión de deuda está controlada; sigue monitoreando los gastos de crédito."
                        ]
                      }
                    }
                    """)))
    @ApiResponse(
        responseCode = "400",
        description = "Error de validación o cuerpo JSON mal formado.",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                name = "Error de validación",
                value = """
                    {
                      "estado": "request_invalido",
                      "errores": [
                        {
                          "campo": "transacciones[0].tipo_pago",
                          "mensaje": "El tipo de pago es obligatorio cuando el tipo de transacción es Egreso"
                        }
                      ]
                    }
                    """)))
	public ResponseEntity<AnalisisFinancieroResponse> analizar(@Valid @RequestBody AnalisisFinancieroRequest request) {
    	return ResponseEntity.ok(analisisFinancieroService.analizar(request));
	}
}
