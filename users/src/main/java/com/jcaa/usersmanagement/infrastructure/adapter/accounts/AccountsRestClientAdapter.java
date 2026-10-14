package com.jcaa.usersmanagement.infrastructure.adapter.accounts;

import com.jcaa.usersmanagement.application.port.out.CreateAccountPort;
import com.jcaa.usersmanagement.domain.exception.AccountCreationException;
import com.jcaa.usersmanagement.domain.model.UserModel;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Paso 5 del diagrama (sincrono): users solicita al microservicio accounts la creacion de la
 * cuenta del usuario recien registrado.
 *
 * <p>Resiliencia aplicada (mismo enfoque del adaptador de Brevo del docente):
 *
 * <ul>
 *   <li><b>Timeout</b>: connect/read configurables en el RestClient.
 *   <li><b>Retry</b>: reintentos ante fallas transitorias (red, timeout, HTTP 5xx).
 *   <li><b>Circuit breaker</b>: si accounts falla repetidamente deja de llamarlo por un tiempo.
 *   <li><b>Fallback</b>: se lanza AccountCreationException y CreateUserService la registra sin
 *       revertir el usuario ya guardado.
 * </ul>
 */
@Slf4j
@Component
public class AccountsRestClientAdapter implements CreateAccountPort {

  static final String CREATE_ACCOUNT_PATH = "/api/accounts";

  private final AccountsClientConfig config;
  private final RestClient restClient;
  private final Retry retry;
  private final CircuitBreaker circuitBreaker;

  public AccountsRestClientAdapter(
      final AccountsClientConfig config,
      @Qualifier("accountsRestClient") final RestClient accountsRestClient) {
    this.config = config;
    this.restClient = accountsRestClient;
    this.retry = buildRetry(config);
    this.circuitBreaker = buildCircuitBreaker(config);
  }

  @Override
  public void createAccountFor(final UserModel user) {
    final CreateAccountRequest request =
        new CreateAccountRequest(
            user.getId().value(), user.getName().value(), user.getEmail().value());
    final Runnable protectedCall =
        Retry.decorateRunnable(
            retry, CircuitBreaker.decorateRunnable(circuitBreaker, () -> post(request)));
    try {
      protectedCall.run();
      log.info("[AccountsRestClientAdapter] cuenta solicitada para el usuario {}.", request.userId());
    } catch (final CallNotPermittedException exception) {
      throw AccountCreationException.becauseAccountsServiceFailed(
          request.userId(), "circuit breaker abierto", exception);
    } catch (final RestClientResponseException exception) {
      throw AccountCreationException.becauseAccountsServiceFailed(
          request.userId(), "HTTP " + exception.getStatusCode().value(), exception);
    } catch (final RestClientException exception) {
      throw AccountCreationException.becauseAccountsServiceFailed(
          request.userId(), exception.getClass().getSimpleName(), exception);
    }
  }

  private void post(final CreateAccountRequest request) {
    restClient
        .post()
        .uri(config.baseUrl() + CREATE_ACCOUNT_PATH)
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve()
        .toBodilessEntity();
  }

  private static boolean isTransientFailure(final Throwable exception) {
    return exception instanceof ResourceAccessException
        || exception instanceof HttpServerErrorException;
  }

  private static Retry buildRetry(final AccountsClientConfig config) {
    final RetryConfig retryConfig =
        RetryConfig.custom()
            .maxAttempts(config.retryMaxAttempts())
            .intervalFunction(IntervalFunction.of(config.retryWait()))
            .retryOnException(AccountsRestClientAdapter::isTransientFailure)
            .build();
    return Retry.of("accounts-client", retryConfig);
  }

  private static CircuitBreaker buildCircuitBreaker(final AccountsClientConfig config) {
    final Predicate<Throwable> transientFailure = AccountsRestClientAdapter::isTransientFailure;
    final CircuitBreakerConfig circuitConfig =
        CircuitBreakerConfig.custom()
            .failureRateThreshold(config.circuitFailureThreshold())
            .slidingWindowSize(config.circuitSlidingWindowSize())
            .minimumNumberOfCalls(config.circuitMinimumCalls())
            .waitDurationInOpenState(config.circuitOpenDuration())
            .recordException(transientFailure)
            .build();
    return CircuitBreaker.of("accounts-client", circuitConfig);
  }
}
