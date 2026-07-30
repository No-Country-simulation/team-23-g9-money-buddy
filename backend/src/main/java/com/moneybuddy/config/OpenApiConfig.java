package com.moneybuddy.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI moneyBuddyOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("API MVP de MoneyBuddy")
                .version("0.0.1")
                .description("API REST sin estado para el MVP de MoneyBuddy. Los endpoints documentados del MVP validan y analizan datos financieros sin persistencia. Swagger UI está disponible en /swagger-ui.html y el JSON OpenAPI en /v3/api-docs."));
    }
}
