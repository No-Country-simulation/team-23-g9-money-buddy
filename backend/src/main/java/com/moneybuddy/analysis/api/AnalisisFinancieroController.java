package com.moneybuddy.analysis.api;

import com.moneybuddy.analysis.application.AnalisisFinancieroService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public final class AnalisisFinancieroController {

  private final AnalisisFinancieroService analisisFinancieroService;

  public AnalisisFinancieroController(AnalisisFinancieroService analisisFinancieroService) {
    this.analisisFinancieroService = analisisFinancieroService;
  }

  @PostMapping("/analisis-financiero")
  public ResponseEntity<AnalisisFinancieroResponse> analizar(@RequestBody AnalisisFinancieroRequest request) {
    return ResponseEntity.ok(analisisFinancieroService.analizar(request));
  }
}
