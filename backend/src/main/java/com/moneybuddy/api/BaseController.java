package com.moneybuddy.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BaseController {

	@GetMapping
	public ApiStatus status() {
		return new ApiStatus("Money Buddy API", "ok");
	}

	public record ApiStatus(String service, String status) {
	}
}
