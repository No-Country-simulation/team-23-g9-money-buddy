package com.moneybuddy.analysis.infrastructure;

import com.moneybuddy.analysis.api.AnalisisFinancieroRequest;
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
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RestClientFinancialScorePredictionAdapterTest {

	@Test
	void predictScoreRoundsHalfUpToNearestInteger() {
		var fixture = fixture();
		fixture.server.expect(requestTo("http://ml-service:8000/predict"))
			.andExpect(method(org.springframework.http.HttpMethod.POST))
			.andExpect(content().json("""
				{
				  "credito_total": 2000,
				  "ingreso_mensual": 3000,
				  "frecuencia_ahorro": "ALTA",
				  "pago_mensual_deudas": 300,
				  "transacciones": [
				    {
				      "tipo": "Egreso",
				      "fecha": "2026-07-01",
				      "descripcion": "Laptop",
				      "tipo_pago": "Credito",
				      "meses_a_deber": 12,
				      "monto": 3000
				    }
				  ]
				}
				"""))
			.andRespond(withSuccess("{\"prediction\":82.5,\"confidence\":null,\"metadata\":{}}", MediaType.APPLICATION_JSON));

		var score = fixture.adapter.predictScore(validRequest());

		assertThat(score).hasValue(83);
		fixture.server.verify();
	}

	@Test
	void predictScoreClampsRoundedScoreToRange() {
		var fixture = fixture();
		fixture.server.expect(requestTo("http://ml-service:8000/predict"))
			.andRespond(withSuccess("{\"prediction\":120.2}", MediaType.APPLICATION_JSON));

		var score = fixture.adapter.predictScore(validRequest());

		assertThat(score).hasValue(100);
		fixture.server.verify();
	}

	@Test
	void predictScoreReturnsEmptyWhenPredictionIsMissing() {
		var fixture = fixture();
		fixture.server.expect(requestTo("http://ml-service:8000/predict"))
			.andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

		var score = fixture.adapter.predictScore(validRequest());

		assertThat(score).isEmpty();
		fixture.server.verify();
	}

	@Test
	void predictScoreReturnsEmptyWhenMlServiceFails() {
		var fixture = fixture();
		fixture.server.expect(requestTo("http://ml-service:8000/predict"))
			.andRespond(withServerError());

		var score = fixture.adapter.predictScore(validRequest());

		assertThat(score).isEmpty();
		fixture.server.verify();
	}

	private Fixture fixture() {
		RestClient.Builder builder = RestClient.builder().baseUrl("http://ml-service:8000");
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		var adapter = new RestClientFinancialScorePredictionAdapter(builder.build());
		return new Fixture(adapter, server);
	}

	private AnalisisFinancieroRequest validRequest() {
		return new AnalisisFinancieroRequest(
			new BigDecimal("2000"),
			new BigDecimal("3000"),
			"alta",
			new BigDecimal("300"),
			List.of(new AnalisisFinancieroRequest.TransaccionRequest(
				"Egreso",
				LocalDate.parse("2026-07-01"),
				"Laptop",
				"Credito",
				12,
				new BigDecimal("3000"))));
	}

	private record Fixture(
		RestClientFinancialScorePredictionAdapter adapter,
		MockRestServiceServer server) {
	}
}
