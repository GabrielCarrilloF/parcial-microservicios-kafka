package com.jcaa.usersmanagement.infrastructure.adapter.accounts;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.enums.UserStatus;
import com.jcaa.usersmanagement.domain.exception.AccountCreationException;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserEmail;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.domain.valueobject.UserName;
import com.jcaa.usersmanagement.domain.valueobject.UserPassword;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@DisplayName("AccountsRestClientAdapter (paso 5)")
class AccountsRestClientAdapterTest {

  private static final String BASE_URL = "http://accounts.test";

  @Test
  @DisplayName("Envia POST /api/accounts con el JSON acordado")
  void shouldPostAccountRequest() {
    final RestClient.Builder builder = RestClient.builder();
    final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    final AccountsRestClientAdapter adapter = new AccountsRestClientAdapter(config(1), builder.build());
    server
        .expect(requestTo(BASE_URL + "/api/accounts"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(
            content()
                .json(
                    "{\"userId\":\"u-01\",\"userName\":\"Ada Lovelace\","
                        + "\"userEmail\":\"ada@example.com\"}"))
        .andRespond(withStatus(HttpStatus.CREATED));

    assertDoesNotThrow(() -> adapter.createAccountFor(user()));
    server.verify();
  }

  @Test
  @DisplayName("Reintenta ante HTTP 5xx y luego aplica el fallback (AccountCreationException)")
  void shouldRetryAndFallback() {
    final RestClient.Builder builder = RestClient.builder();
    final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    final AccountsRestClientAdapter adapter = new AccountsRestClientAdapter(config(2), builder.build());
    server.expect(times(2), requestTo(BASE_URL + "/api/accounts")).andRespond(withServerError());

    final AccountCreationException exception =
        assertThrows(AccountCreationException.class, () -> adapter.createAccountFor(user()));

    assertTrue(exception.getMessage().contains("HTTP 500"));
    server.verify();
  }

  @Test
  @DisplayName("AccountsClientConfig quita la barra final y valida la URL")
  void shouldValidateConfig() {
    assertEquals(
        "http://x",
        new AccountsClientConfig("http://x/", null, null, 1, null, 50, 10, 5, null).baseUrl());
    assertThrows(
        IllegalArgumentException.class,
        () -> new AccountsClientConfig(" ", null, null, 1, null, 50, 10, 5, null));
  }

  private static AccountsClientConfig config(final int attempts) {
    return new AccountsClientConfig(
        BASE_URL,
        Duration.ofMillis(100),
        Duration.ofMillis(100),
        attempts,
        Duration.ofMillis(1),
        50,
        10,
        5,
        Duration.ofSeconds(1));
  }

  private static UserModel user() {
    return new UserModel(
        new UserId("u-01"),
        new UserName("Ada Lovelace"),
        new UserEmail("ada@example.com"),
        UserPassword.fromPlainText("Pass1234"),
        UserRole.MEMBER,
        UserStatus.PENDING);
  }
}
