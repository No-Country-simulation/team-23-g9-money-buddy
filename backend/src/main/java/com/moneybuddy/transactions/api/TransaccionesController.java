package com.moneybuddy.transactions.api;

import com.moneybuddy.api.GlobalExceptionHandler.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/transacciones")
@Tag(name = "Transacciones", description = "Endpoint sin estado del MVP para validar y clasificar transacciones.")
public final class TransaccionesController {
    private final TransaccionesService service;

    public TransaccionesController(TransaccionesService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
        summary = "Clasificar transacciones",
        description = "Valida y clasifica las transacciones enviadas sin persistencia. tipo_pago es obligatorio solo cuando tipo es Egreso. meses_a_deber es obligatorio cuando tipo_pago es Credito.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        content = @Content(schema = @Schema(implementation = TransaccionesRequest.class), examples = @ExampleObject(value = """
            {
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
                }
              ]
            }
            """)))
    @ApiResponse(responseCode = "200", description = "Transacciones clasificadas correctamente.", content = @Content(schema = @Schema(implementation = TransaccionesResponse.class), examples = @ExampleObject(name = "Transacciones clasificadas", value = """
        {
          "success": true,
          "message": "Transacciones clasificadas correctamente",
          "data": {
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
              }
            ]
          }
        }
        """)))
    @ApiResponse(responseCode = "400", description = "Error de validación o cuerpo JSON mal formado.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<TransaccionesResponse> clasificar(@Valid @RequestBody TransaccionesRequest request) {
        return ResponseEntity.ok(service.clasificar(request));
    }
}
