package com.moneybuddy.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Estado de la API", description = "Endpoint básico de estado del servicio.")
public class BaseController {

	@GetMapping
	@Operation(summary = "Obtener estado de la API", description = "Devuelve una respuesta simple y sin estado con el estado del servicio.")
	@ApiResponse(responseCode = "200", description = "La API está disponible.", content = @Content(schema = @Schema(implementation = ApiStatus.class)))
	public ApiStatus status() {
		return new ApiStatus("Money Buddy API", "ok");
	}

	@Schema(description = "Respuesta de estado del servicio.")
	public record ApiStatus(
		@Schema(description = "Nombre visible del servicio.", example = "Money Buddy API") String service,
		@Schema(description = "Estado de salud del servicio.", example = "ok") String status) {
	}
}
