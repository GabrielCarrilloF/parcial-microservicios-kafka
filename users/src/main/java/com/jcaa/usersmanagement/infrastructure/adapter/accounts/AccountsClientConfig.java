package com.jcaa.usersmanagement.infrastructure.adapter.accounts;

import java.time.Duration;

/**
 * Configuracion del cliente HTTP hacia el microservicio accounts (paso 5).
 *
 * <p>Incluye los parametros de resiliencia: timeouts, reintentos y circuit breaker.
 */
public record AccountsClientConfig(
    String baseUrl,
    Duration connectTimeout,
    Duration readTimeout,
    int retryMaxAttempts,
    Duration retryWait,
    int circuitFailureThreshold,
    int circuitSlidingWindowSize,
    int circuitMinimumCalls,
    Duration circuitOpenDuration) {

  public AccountsClientConfig {
    if (baseUrl == null || baseUrl.isBlank()) {
      throw new IllegalArgumentException("ACCOUNTS_BASE_URL es obligatorio.");
    }
    if (retryMaxAttempts < 1) {
      throw new IllegalArgumentException("accounts.retry.max-attempts debe ser mayor o igual a 1.");
    }
    baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
  }
}
