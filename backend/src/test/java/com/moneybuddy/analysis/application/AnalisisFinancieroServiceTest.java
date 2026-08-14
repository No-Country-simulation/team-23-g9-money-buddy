package com.moneybuddy.analysis.application;

import com.moneybuddy.analysis.api.AnalisisFinancieroRequest;
import com.moneybuddy.classification.application.DeterministicTransactionClassifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnalisisFinancieroServiceTest {

	@Test
	void analizarUsesMlScoreWhenAvailable() {
		AnalisisFinancieroService service = new AnalisisFinancieroService(
			request -> OptionalInt.of(82),
			transactions -> Optional.empty(),
			new DeterministicTransactionClassifier());

		var response = service.analizar(validRequest());

		assertThat(response.data().scoreFinanciero()).isEqualTo(82);
		assertThat(response.data().perfilFinanciero()).isEqualTo("estable");
	}

	@Test
	void analizarFallsBackToDeterministicScoreWhenMlScoreIsUnavailable() {
		AnalisisFinancieroService service = new AnalisisFinancieroService(
			request -> OptionalInt.empty(),
			transactions -> Optional.empty(),
			new DeterministicTransactionClassifier());

		var response = service.analizar(validRequest());

		assertThat(response.data().scoreFinanciero()).isEqualTo(85);
		assertThat(response.data().perfilFinanciero()).isEqualTo("estable");
	}

	@Test
	void analizarKeepsDeterministicCategoriesWhenRulesMatchAndUsesMlForOtros() {
		AnalisisFinancieroService service = new AnalisisFinancieroService(
			request -> OptionalInt.empty(),
			transactions -> Optional.of(List.of("salud", "tecnologia", "ingreso")),
			new DeterministicTransactionClassifier());

		var response = service.analizar(validRequest());

		assertThat(response.data().transaccionesClasificadas())
			.extracting("categoria")
			.containsExactly("alimentos", "transporte", "ingreso");
		assertThat(response.data().resumenGastos()).containsEntry("alimentos", new BigDecimal("200"));
		assertThat(response.data().resumenGastos()).containsEntry("transporte", new BigDecimal("450"));
	}

	@Test
	void analizarUsesMlCategoryWhenDeterministicCategoryIsOtros() {
		AnalisisFinancieroService service = new AnalisisFinancieroService(
			request -> OptionalInt.empty(),
			transactions -> Optional.of(List.of("servicios")),
			new DeterministicTransactionClassifier());

		var response = service.analizar(new AnalisisFinancieroRequest(
			new BigDecimal("2000"),
			new BigDecimal("3000"),
			"ALTA",
			new BigDecimal("300"),
			List.of(new AnalisisFinancieroRequest.TransaccionRequest(
				"Egreso",
				LocalDate.parse("2026-07-20"),
				"Pago misterioso",
				"Debito",
				null,
				new BigDecimal("200")))));

		assertThat(response.data().transaccionesClasificadas())
			.extracting("categoria")
			.containsExactly("servicios");
		assertThat(response.data().resumenGastos()).containsEntry("servicios", new BigDecimal("200"));
	}

	@Test
	void analizarFallsBackToDeterministicCategoriesWhenMlCategoriesAreUnavailable() {
		AnalisisFinancieroService service = new AnalisisFinancieroService(
			request -> OptionalInt.empty(),
			transactions -> Optional.empty(),
			new DeterministicTransactionClassifier());

		var response = service.analizar(validRequest());

		assertThat(response.data().transaccionesClasificadas())
			.extracting("categoria")
			.containsExactly("alimentos", "transporte", "ingreso");
	}

	private AnalisisFinancieroRequest validRequest() {
		return new AnalisisFinancieroRequest(
			new BigDecimal("2000"),
			new BigDecimal("3000"),
			"ALTA",
			new BigDecimal("300"),
			List.of(
				new AnalisisFinancieroRequest.TransaccionRequest(
					"Egreso",
					LocalDate.parse("2026-07-20"),
					"Supermercado",
					"Debito",
					null,
					new BigDecimal("200")),
				new AnalisisFinancieroRequest.TransaccionRequest(
					"Egreso",
					LocalDate.parse("2026-07-21"),
					"Taxi",
					"Credito",
					3,
					new BigDecimal("450")),
				new AnalisisFinancieroRequest.TransaccionRequest(
					"Ingreso",
					LocalDate.parse("2026-07-01"),
					"Salary",
					null,
					null,
					new BigDecimal("3000"))));
	}
}
