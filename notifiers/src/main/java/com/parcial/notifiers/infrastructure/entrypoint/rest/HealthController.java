package com.parcial.notifiers.infrastructure.entrypoint.rest;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Ruta de estado usada por Render para verificar que el servicio esta activo. */
@RestController
public class HealthController {

  @GetMapping("/")
  public Map<String, String> health() {
    return Map.of("service", "notifiers", "status", "UP");
  }
}
