package com.moneybuddy.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

class CorsConfigTest {

	@Test
	void allowsLocalFrontendOriginsByDefault() {
		CorsConfiguration configuration = corsConfigurationFor(new CorsProperties(List.of(
			"http://localhost:5173",
			"http://localhost:5174"
		)));

		assertThat(configuration.getAllowedOrigins())
			.containsExactly("http://localhost:5173", "http://localhost:5174");
	}

	@Test
	void allowsConfiguredDeploymentOrigin() {
		CorsConfiguration configuration = corsConfigurationFor(new CorsProperties(List.of(
			"https://money-buddy.example.com"
		)));

		assertThat(configuration.getAllowedOrigins())
			.containsExactly("https://money-buddy.example.com");
	}

	private CorsConfiguration corsConfigurationFor(CorsProperties properties) {
		TestCorsRegistry registry = new TestCorsRegistry();
		new CorsConfig(properties).addCorsMappings(registry);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.setCorsConfigurations(registry.corsConfigurations());

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api");
		request.addHeader("Origin", "https://money-buddy.example.com");

		return source.getCorsConfiguration(request);
	}

	private static final class TestCorsRegistry extends CorsRegistry {

		java.util.Map<String, CorsConfiguration> corsConfigurations() {
			return getCorsConfigurations();
		}
	}
}
