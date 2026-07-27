package com.moneybuddy.analysis.api;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public final class FinancialTransactionValidator
	implements ConstraintValidator<ValidFinancialTransaction, AnalisisFinancieroRequest.TransaccionRequest> {
	
	@Override
	public boolean isValid(AnalisisFinancieroRequest.TransaccionRequest transaccion, ConstraintValidatorContext contexto) {
    	if (transaccion == null) {
      		return true;
    	}

    	boolean valido = true;
    	contexto.disableDefaultConstraintViolation();

		boolean tipoPagoRequeridoFaltante = esEgreso(transaccion.tipo()) && isBlank(transaccion.tipoPago());

		if (tipoPagoRequeridoFaltante) {
			agregarInfraccion(contexto, "tipo_pago", "El tipo de pago es obligatorio cuando el tipo de transacción es Egreso");
			valido = false;
		}

		if (!tipoPagoRequeridoFaltante && transaccion.tipoPago() != null && !esTipoPagoValido(transaccion.tipoPago())) {
			agregarInfraccion(contexto, "tipo_pago", "El tipo de pago debe ser Efectivo, Debito o Credito");
			valido = false;
		}

		if (esCredito(transaccion.tipoPago()) && transaccion.mesesADeber() == null) {
			agregarInfraccion(contexto, "mesesADeber", "Los meses a deber son obligatorios cuando el tipo de pago es Credito");
			valido = false;
		}

    	return valido;
  	}

  	private static void agregarInfraccion(ConstraintValidatorContext context, String field, String message) {
    	context.buildConstraintViolationWithTemplate(message).addPropertyNode(field).addConstraintViolation();
  	}

	private static boolean esEgreso(String tipo) {
		return "Egreso".equalsIgnoreCase(tipo);
	}

	private static boolean esCredito(String tipoPago) {
		return "Credito".equalsIgnoreCase(tipoPago);
	}

	private static boolean esTipoPagoValido(String tipoPago) {
		return "Efectivo".equalsIgnoreCase(tipoPago)
			|| "Debito".equalsIgnoreCase(tipoPago)
			|| "Credito".equalsIgnoreCase(tipoPago);
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
