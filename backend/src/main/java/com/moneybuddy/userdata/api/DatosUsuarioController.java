package com.moneybuddy.userdata.api;

import com.moneybuddy.api.GlobalExceptionHandler.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@Tag(name = "Datos financieros del usuario", description = "Endpoints sin estado del MVP para validar datos del perfil financiero del usuario.")
public final class DatosUsuarioController {
    
    @PostMapping("/datos-usuario")
    @Operation(summary = "Validar datos financieros del usuario", description = "Valida los datos del perfil financiero del usuario y devuelve los valores aceptados. El endpoint del MVP no tiene estado y no persiste datos.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        content = @Content(schema = @Schema(implementation = DatosUsuarioRequest.class), examples = @ExampleObject(value = """
            {
              "credito_total": 15000,
              "ingreso_mensual": 4500,
              "frecuencia_ahorro": "MEDIA"
            }
            """)))
    @ApiResponse(responseCode = "200", description = "Datos financieros validados correctamente.", content = @Content(schema = @Schema(implementation = DatosUsuarioResponse.class), examples = @ExampleObject(name = "Datos de usuario validados", value = """
        {
          "success": true,
          "message": "Datos financieros validados correctamente",
          "data": {
            "credito_total": 15000,
            "ingreso_mensual": 4500,
            "frecuencia_ahorro": "MEDIA"
          }
        }
        """)))
    @ApiResponse(responseCode = "400", description = "Error de validación o cuerpo JSON mal formado.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<DatosUsuarioResponse> guardarDatosUsuario(@Valid @RequestBody DatosUsuarioRequest request) {
        return ResponseEntity.ok(validarDatosUsuario(request));
    }

    @PutMapping("/datos-usuario")
    @Operation(summary = "Validar datos financieros actualizados del usuario", description = "Valida los datos actualizados del perfil financiero del usuario y devuelve los valores aceptados. El endpoint del MVP no tiene estado y no persiste datos.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(schema = @Schema(implementation = DatosUsuarioRequest.class)))
    @ApiResponse(responseCode = "200", description = "Datos financieros validados correctamente.", content = @Content(schema = @Schema(implementation = DatosUsuarioResponse.class), examples = @ExampleObject(name = "Datos de usuario actualizados", value = """
        {
          "success": true,
          "message": "Datos financieros validados correctamente",
          "data": {
            "credito_total": 18000,
            "ingreso_mensual": 5200,
            "frecuencia_ahorro": "ALTA"
          }
        }
        """)))
    @ApiResponse(responseCode = "400", description = "Error de validación o cuerpo JSON mal formado.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<DatosUsuarioResponse> actualizarDatosUsuario(@Valid @RequestBody DatosUsuarioRequest request) {
        return ResponseEntity.ok(validarDatosUsuario(request));
    }

    private DatosUsuarioResponse validarDatosUsuario(DatosUsuarioRequest request) {
        return new DatosUsuarioResponse(
            true, 
            "Datos financieros validados correctamente", 
            new DatosUsuarioResponse.Data(
                request.creditoTotal(), 
                request.ingresoMensual(), 
                request.frecuenciaAhorro()
            )
        );
    }
} 
