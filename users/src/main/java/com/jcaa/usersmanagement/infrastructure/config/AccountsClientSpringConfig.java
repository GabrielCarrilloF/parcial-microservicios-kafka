package com.jcaa.usersmanagement.infrastructure.config;

import com.jcaa.usersmanagement.infrastructure.adapter.accounts.AccountsClientConfig;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/** Beans del cliente HTTP que users usa para comunicarse con accounts (paso 5). */
@Configuration(proxyBeanMethods = false)
public class AccountsClientSpringConfig {

  @Bean
  public AccountsClientConfig accountsClientConfig(
      @Value("${accounts.base-url:http://localhost:8081}") final String baseUrl,
      @Value("${accounts.timeout.connect:5s}") final Duration connectTimeout,
      @Value("${accounts.timeout.read:60s}") final Duration readTimeout,
      @Value("${accounts.retry.max-attempts:3}") final int retryMaxAttempts,
      @Value("${accounts.retry.wait:2s}") final Duration retryWait,
      @Value("${accounts.circuit-breaker.failure-rate-threshold:50}") final int failureThreshold,
      @Value("${accounts.circuit-breaker.sliding-window-size:10}") final int slidingWindowSize,
      @Value("${accounts.circuit-breaker.minimum-calls:5}") final int minimumCalls,
      @Value("${accounts.circuit-breaker.open-duration:30s}") final Duration openDuration) {
    return new AccountsClientConfig(
        baseUrl,
        connectTimeout,
        readTimeout,
        retryMaxAttempts,
        retryWait,
        failureThreshold,
        slidingWindowSize,
        minimumCalls,
        openDuration);
  }

  @Bean
  public RestClient accountsRestClient(final AccountsClientConfig config) {
    final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(config.connectTimeout());
    requestFactory.setReadTimeout(config.readTimeout());
    return RestClient.builder().requestFactory(requestFactory).build();
  }
}
