package com.moneybuddy.transactions.api;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public final class TransactionValidator implements ConstraintValidator<ValidTransaction, TransaccionesRequest.TransaccionRequest> {

    @Override
    public boolean isValid(TransaccionesRequest.TransaccionRequest transaccion, ConstraintValidatorContext context) {
        if (transaccion == null) {
            return true;
        }

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        boolean missingRequiredPaymentType = isEgreso(transaccion.tipo()) && isBlank(transaccion.tipoPago());

        if (missingRequiredPaymentType) {
            addViolation(context, "tipo_pago", "El tipo de pago es obligatorio cuando el tipo de transacción es Egreso");
            valid = false;
        }

        if (!missingRequiredPaymentType && transaccion.tipoPago() != null && !isValidPaymentType(transaccion.tipoPago())) {
            addViolation(context, "tipo_pago", "El tipo de pago debe ser Efectivo, Debito o Credito");
            valid = false;
        }

        if (isCredit(transaccion.tipoPago()) && transaccion.mesesADeber() == null) {
            addViolation(context, "mesesADeber", "Los meses a deber son obligatorios cuando el tipo de pago es Credito");
            valid = false;
        }

        return valid;
    }

    private static void addViolation(ConstraintValidatorContext context, String field, String message) {
        context.buildConstraintViolationWithTemplate(message).addPropertyNode(field).addConstraintViolation();
    }

    private static boolean isEgreso(String tipo) {
        return "Egreso".equals(tipo);
    }

    private static boolean isCredit(String tipoPago) {
        return "Credito".equals(tipoPago);
    }

    private static boolean isValidPaymentType(String tipoPago) {
        return "Efectivo".equals(tipoPago)
            || "Debito".equals(tipoPago)
            || "Credito".equals(tipoPago);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
