package com.moneybuddy.classification.application;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionClassificationInput(
	String tipo,
	LocalDate fecha,
	String descripcion,
	String tipoPago,
	Integer mesesADeber,
	BigDecimal monto) {
}
