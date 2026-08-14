package com.moneybuddy.transactions.api;

import com.moneybuddy.classification.application.DeterministicTransactionClassifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TransaccionesServiceTest {

	@Test
	void clasificarKeepsDeterministicCategoriesWhenRulesMatchAndUsesMlForOtros() {
		var service = new TransaccionesService(
			transactions -> Optional.of(List.of("salud", "ingreso")),
			new DeterministicTransactionClassifier());

		var response = service.clasificar(validRequest());

		assertThat(response.data().transaccionesClasificadas())
			.extracting("categoria")
			.containsExactly("alimentos", "ingreso");
	}

	@Test
	void clasificarUsesMlCategoryWhenDeterministicCategoryIsOtros() {
		var service = new TransaccionesService(
			transactions -> Optional.of(List.of("servicios", "ingreso")),
			new DeterministicTransactionClassifier());

		var response = service.clasificar(new TransaccionesRequest(List.of(
			new TransaccionesRequest.TransaccionRequest(
				"Egreso",
				LocalDate.parse("2026-07-20"),
				"Pago misterioso",
				"Debito",
				null,
				new BigDecimal("200")),
			new TransaccionesRequest.TransaccionRequest(
				"Ingreso",
				LocalDate.parse("2026-07-01"),
				"Salary",
				null,
				null,
				new BigDecimal("3000")))));

		assertThat(response.data().transaccionesClasificadas())
			.extracting("categoria")
			.containsExactly("servicios", "ingreso");
	}

	@Test
	void clasificarFallsBackToDeterministicCategoriesWhenMlCategoriesAreUnavailable() {
		var service = new TransaccionesService(
			transactions -> Optional.empty(),
			new DeterministicTransactionClassifier());

		var response = service.clasificar(validRequest());

		assertThat(response.data().transaccionesClasificadas())
			.extracting("categoria")
			.containsExactly("alimentos", "ingreso");
	}

	private TransaccionesRequest validRequest() {
		return new TransaccionesRequest(List.of(
			new TransaccionesRequest.TransaccionRequest(
				"Egreso",
				LocalDate.parse("2026-07-20"),
				"Supermercado",
				"Debito",
				null,
				new BigDecimal("200")),
			new TransaccionesRequest.TransaccionRequest(
				"Ingreso",
				LocalDate.parse("2026-07-01"),
				"Salary",
				null,
				null,
				new BigDecimal("3000"))));
	}
}
