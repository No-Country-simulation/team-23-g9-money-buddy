package com.moneybuddy.api;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public final class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
    List<ValidationError> errores = exception.getBindingResult().getFieldErrors().stream()
        .map(error -> new ValidationError(toJsonField(error), error.getDefaultMessage()))
        .toList();

    return new ErrorResponse("request_invalido", errores);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
    return new ErrorResponse(
        "request_invalido",
        List.of(new ValidationError("body", "El cuerpo de la solicitud debe ser un JSON válido")));
  }

  private static String toJsonField(FieldError error) {
    return error.getField()
        .replace("ingresoMensual", "ingreso_mensual")
        .replace("creditoTotal", "credito_total")
        .replace("frecuenciaAhorro", "frecuencia_ahorro")
        .replace("pagoMensualDeudas", "pago_mensual_deudas")
        .replace("tipoPago", "tipo_pago")
        .replace("mesesADeber", "meses_a_deber");
  }

  @Schema(description = "Respuesta estándar de error de validación.")
  public record ErrorResponse(
      @Schema(description = "Estado de error legible por máquina.", example = "request_invalido") String estado,
      @ArraySchema(schema = @Schema(implementation = ValidationError.class)) List<ValidationError> errores) {
  }

  @Schema(description = "Error de validación individual.")
  public record ValidationError(
      @Schema(description = "Campo JSON que no superó la validación.", example = "transacciones[0].tipo_pago") String campo,
      @Schema(description = "Mensaje de validación.", example = "El tipo de pago es obligatorio cuando el tipo de transacción es Egreso") String mensaje) {
  }
}
