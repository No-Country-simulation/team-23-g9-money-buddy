package com.moneybuddy.classification.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.moneybuddy.analysis.infrastructure.MlServiceProperties;
import com.moneybuddy.classification.application.TransactionClassificationInput;
import com.moneybuddy.classification.application.TransactionClassificationPort;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public final class RestClientTransactionClassificationAdapter implements TransactionClassificationPort {

	private static final Set<String> OFFICIAL_CATEGORIES = Set.of(
		"alimentos",
		"transporte",
		"salud",
		"vivienda",
		"educacion",
		"ocio_entretenimiento",
		"servicios",
		"ropa_calzado",
		"tecnologia",
		"otros",
		"ingreso");

	private final RestClient restClient;

	@Autowired
	public RestClientTransactionClassificationAdapter(RestClient.Builder restClientBuilder, MlServiceProperties properties) {
		this(restClientBuilder
			.requestFactory(simpleHttpRequestFactory())
			.baseUrl(properties.baseUrl())
			.build());
	}

	RestClientTransactionClassificationAdapter(RestClient restClient) {
		this.restClient = restClient;
	}

	private static SimpleClientHttpRequestFactory simpleHttpRequestFactory() {
		var requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofSeconds(2));
		requestFactory.setReadTimeout(Duration.ofSeconds(5));
		return requestFactory;
	}

	@Override
	public Optional<List<String>> classify(List<TransactionClassificationInput> transactions) {
		try {
			MlClassificationResponse response = restClient.post()
				.uri("/classify-transactions")
				.body(MlClassificationRequest.from(transactions))
				.retrieve()
				.body(MlClassificationResponse.class);

			return categoriesFrom(response, transactions.size());
		} catch (RuntimeException exception) {
			return Optional.empty();
		}
	}

	private Optional<List<String>> categoriesFrom(MlClassificationResponse response, int expectedSize) {
		if (response == null || response.transacciones() == null || response.transacciones().size() != expectedSize) {
			return Optional.empty();
		}

		List<String> categories = response.transacciones().stream()
			.map(MlClassifiedTransactionResponse::categoria)
			.toList();

		return categories.stream().anyMatch(category -> category == null || category.isBlank() || !OFFICIAL_CATEGORIES.contains(category))
			? Optional.empty()
			: Optional.of(categories);
	}

	private record MlClassificationRequest(
		@JsonProperty("transacciones") List<MlTransactionRequest> transacciones) {

		private static MlClassificationRequest from(List<TransactionClassificationInput> transactions) {
			return new MlClassificationRequest(transactions.stream()
				.map(MlTransactionRequest::from)
				.toList());
		}
	}

	private record MlTransactionRequest(
		@JsonProperty("tipo") String tipo,
		@JsonProperty("fecha") String fecha,
		@JsonProperty("descripcion") String descripcion,
		@JsonProperty("tipo_pago") String tipoPago,
		@JsonProperty("meses_a_deber") Integer mesesADeber,
		@JsonProperty("monto") BigDecimal monto) {

		private static MlTransactionRequest from(TransactionClassificationInput transaction) {
			return new MlTransactionRequest(
				transaction.tipo(),
				transaction.fecha().toString(),
				transaction.descripcion(),
				transaction.tipoPago(),
				transaction.mesesADeber(),
				transaction.monto());
		}
	}

	private record MlClassificationResponse(
		@JsonProperty("transacciones") List<MlClassifiedTransactionResponse> transacciones) {
	}

	private record MlClassifiedTransactionResponse(
		@JsonProperty("categoria") String categoria) {
	}
}
