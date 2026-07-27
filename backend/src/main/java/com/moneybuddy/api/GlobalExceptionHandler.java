package com.moneybuddy.api;

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

  public record ErrorResponse(String estado, List<ValidationError> errores) {
  }

  public record ValidationError(String campo, String mensaje) {
  }
}
