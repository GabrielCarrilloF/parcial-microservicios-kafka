package com.parcial.accounts.infrastructure.entrypoint.rest.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ruta publica "/" usada por Render (healthCheckPath) y para "despertar" el servicio antes de la
 * demostracion (en el plan gratuito se duerme tras 15 minutos sin trafico).
 */
@RestController
public class HealthController {

  @GetMapping("/")
  public Map<String, String> health() {
    return Map.of("service", "accounts", "status", "UP");
  }
}
