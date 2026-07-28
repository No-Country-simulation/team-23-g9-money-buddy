package com.moneybuddy.transactions.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/transacciones")
public final class TransaccionesController {
    private final TransaccionesService service;

    public TransaccionesController(TransaccionesService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TransaccionesResponse> clasificar(@Valid @RequestBody TransaccionesRequest request) {
        return ResponseEntity.ok(service.clasificar(request));
    }
}
