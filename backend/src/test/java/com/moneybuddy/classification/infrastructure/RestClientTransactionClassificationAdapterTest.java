package com.moneybuddy.classification.infrastructure;

import com.moneybuddy.classification.application.TransactionClassificationInput;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RestClientTransactionClassificationAdapterTest {

	@Test
	void classifySendsExpectedJsonAndReturnsCategoriesInOrder() {
		var fixture = fixture();
		fixture.server.expect(requestTo("http://ml-service:8000/classify-transactions"))
			.andExpect(method(org.springframework.http.HttpMethod.POST))
			.andExpect(content().json("""
				{
				  "transacciones": [
				    {
				      "tipo": "Egreso",
				      "fecha": "2026-07-20",
				      "descripcion": "Supermercado",
				      "tipo_pago": "Debito",
				      "meses_a_deber": null,
				      "monto": 200
				    },
				    {
				      "tipo": "Ingreso",
				      "fecha": "2026-07-01",
				      "descripcion": "Salary",
				      "tipo_pago": null,
				      "meses_a_deber": null,
				      "monto": 3000
				    }
				  ]
				}
				"""))
			.andRespond(withSuccess("""
				{
				  "transacciones": [
				    {"descripcion":"Supermercado","categoria":"salud"},
				    {"descripcion":"Salary","categoria":"ingreso"}
				  ]
				}
				""", MediaType.APPLICATION_JSON));

		var categories = fixture.adapter.classify(transactions());

		assertThat(categories).hasValue(List.of("salud", "ingreso"));
		fixture.server.verify();
	}

	@Test
	void classifyReturnsEmptyWhenResponseSizeDoesNotMatchRequestSize() {
		var fixture = fixture();
		fixture.server.expect(requestTo("http://ml-service:8000/classify-transactions"))
			.andRespond(withSuccess("{" +
				"\"transacciones\":[{\"categoria\":\"salud\"}]}"
				, MediaType.APPLICATION_JSON));

		var categories = fixture.adapter.classify(transactions());

		assertThat(categories).isEmpty();
		fixture.server.verify();
	}

	@Test
	void classifyReturnsEmptyWhenMlServiceReturnsUnavailable() {
		var fixture = fixture();
		fixture.server.expect(requestTo("http://ml-service:8000/classify-transactions"))
			.andRespond(withStatus(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE));

		var categories = fixture.adapter.classify(transactions());

		assertThat(categories).isEmpty();
		fixture.server.verify();
	}

	@Test
	void classifyReturnsEmptyWhenAnyCategoryIsBlank() {
		var fixture = fixture();
		fixture.server.expect(requestTo("http://ml-service:8000/classify-transactions"))
			.andRespond(withSuccess("""
				{"transacciones":[{"categoria":"salud"},{"categoria":" "}]}
				""", MediaType.APPLICATION_JSON));

		var categories = fixture.adapter.classify(transactions());

		assertThat(categories).isEmpty();
		fixture.server.verify();
	}

	@Test
	void classifyReturnsEmptyWhenAnyCategoryIsNotOfficial() {
		var fixture = fixture();
		fixture.server.expect(requestTo("http://ml-service:8000/classify-transactions"))
			.andRespond(withSuccess("""
				{"transacciones":[{"categoria":"salud"},{"categoria":"compras"}]}
				""", MediaType.APPLICATION_JSON));

		var categories = fixture.adapter.classify(transactions());

		assertThat(categories).isEmpty();
		fixture.server.verify();
	}

	private Fixture fixture() {
		RestClient.Builder builder = RestClient.builder().baseUrl("http://ml-service:8000");
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		var adapter = new RestClientTransactionClassificationAdapter(builder.build());
		return new Fixture(adapter, server);
	}

	private List<TransactionClassificationInput> transactions() {
		return List.of(
			new TransactionClassificationInput(
				"Egreso",
				LocalDate.parse("2026-07-20"),
				"Supermercado",
				"Debito",
				null,
				new BigDecimal("200")),
			new TransactionClassificationInput(
				"Ingreso",
				LocalDate.parse("2026-07-01"),
				"Salary",
				null,
				null,
				new BigDecimal("3000")));
	}

	private record Fixture(
		RestClientTransactionClassificationAdapter adapter,
		MockRestServiceServer server) {
	}
}
