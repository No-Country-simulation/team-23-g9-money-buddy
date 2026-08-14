package com.moneybuddy.analysis.infrastructure;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "moneybuddy.ml-service")
public record MlServiceProperties(@NotBlank String baseUrl) {
}
