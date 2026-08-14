package com.moneybuddy.analysis.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.moneybuddy.analysis.api.AnalisisFinancieroRequest;
import com.moneybuddy.analysis.application.port.FinancialScorePredictionPort;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.OptionalInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public final class RestClientFinancialScorePredictionAdapter implements FinancialScorePredictionPort {

	private final RestClient restClient;

	@Autowired
	public RestClientFinancialScorePredictionAdapter(RestClient.Builder restClientBuilder, MlServiceProperties properties) {
		this(restClientBuilder
			.requestFactory(simpleHttpRequestFactory())
			.baseUrl(properties.baseUrl())
			.build());
	}

	RestClientFinancialScorePredictionAdapter(RestClient restClient) {
		this.restClient = restClient;
	}

	private static SimpleClientHttpRequestFactory simpleHttpRequestFactory() {
		var requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofSeconds(2));
		requestFactory.setReadTimeout(Duration.ofSeconds(5));
		return requestFactory;
	}

	@Override
	public OptionalInt predictScore(AnalisisFinancieroRequest request) {
		try {
			MlPredictionResponse response = restClient.post()
				.uri("/predict")
				.body(MlPredictionRequest.from(request))
				.retrieve()
				.body(MlPredictionResponse.class);

			return response == null ? OptionalInt.empty() : roundNearestIntegerClampedToScoreRange(response.prediction());
		} catch (RuntimeException exception) {
			return OptionalInt.empty();
		}
	}

	private OptionalInt roundNearestIntegerClampedToScoreRange(Double prediction) {
		if (prediction == null || !Double.isFinite(prediction)) {
			return OptionalInt.empty();
		}

		int roundedScore = BigDecimal.valueOf(prediction)
			.setScale(0, RoundingMode.HALF_UP)
			.intValue();
		return OptionalInt.of(Math.max(0, Math.min(100, roundedScore)));
	}

	private record MlPredictionResponse(Double prediction) {
	}

	private record MlPredictionRequest(
		@JsonProperty("credito_total") BigDecimal creditoTotal,
		@JsonProperty("ingreso_mensual") BigDecimal ingresoMensual,
		@JsonProperty("frecuencia_ahorro") String frecuenciaAhorro,
		@JsonProperty("pago_mensual_deudas") BigDecimal pagoMensualDeudas,
		@JsonProperty("transacciones") List<MlTransactionRequest> transacciones) {

		private static MlPredictionRequest from(AnalisisFinancieroRequest request) {
			return new MlPredictionRequest(
				request.creditoTotal(),
				request.ingresoMensual(),
				normalizedFrequency(request.frecuenciaAhorro()),
				request.pagoMensualDeudas(),
				request.transacciones().stream()
					.map(MlTransactionRequest::from)
					.toList());
		}

		private static String normalizedFrequency(String frequency) {
			return frequency == null ? null : frequency.toUpperCase(Locale.ROOT);
		}
	}

	private record MlTransactionRequest(
		@JsonProperty("tipo") String tipo,
		@JsonProperty("fecha") String fecha,
		@JsonProperty("descripcion") String descripcion,
		@JsonProperty("tipo_pago") String tipoPago,
		@JsonProperty("meses_a_deber") Integer mesesADeber,
		@JsonProperty("monto") BigDecimal monto) {

		private static MlTransactionRequest from(AnalisisFinancieroRequest.TransaccionRequest transaction) {
			return new MlTransactionRequest(
				transaction.tipo(),
				transaction.fecha().toString(),
				transaction.descripcion(),
				transaction.tipoPago(),
				transaction.mesesADeber(),
				transaction.monto());
		}
	}
}
