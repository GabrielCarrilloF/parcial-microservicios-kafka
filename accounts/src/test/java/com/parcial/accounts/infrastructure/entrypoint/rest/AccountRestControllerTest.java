package com.parcial.accounts.infrastructure.entrypoint.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.parcial.accounts.application.dto.CreateAccountResult;
import com.parcial.accounts.application.port.in.CreateAccountUseCase;
import com.parcial.accounts.application.port.in.GetAccountsUseCase;
import com.parcial.accounts.domain.exception.AccountNotFoundException;
import com.parcial.accounts.domain.model.AccountModel;
import com.parcial.accounts.infrastructure.entrypoint.rest.advice.GlobalExceptionHandler;
import com.parcial.accounts.infrastructure.entrypoint.rest.controller.AccountRestController;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@DisplayName("AccountRestController (paso 5)")
class AccountRestControllerTest {

  private static final String BODY =
      "{\"userId\":\"u-01\",\"userName\":\"Ada Lovelace\",\"userEmail\":\"ada@example.com\"}";

  private final CreateAccountUseCase createAccountUseCase = mock(CreateAccountUseCase.class);
  private final GetAccountsUseCase getAccountsUseCase = mock(GetAccountsUseCase.class);
  private final AccountModel account =
      AccountModel.openFor("u-01", "Ada Lovelace", "ada@example.com", "AC-0000000001");
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(
                new AccountRestController(createAccountUseCase, getAccountsUseCase))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  @DisplayName("POST /api/accounts responde 201 cuando la cuenta es nueva")
  void shouldReturn201WhenCreated() throws Exception {
    when(createAccountUseCase.execute(any())).thenReturn(new CreateAccountResult(account, true));

    mockMvc
        .perform(
            post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.accountNumber").value("AC-0000000001"))
        .andExpect(jsonPath("$.status").value("ACTIVE"));
  }

  @Test
  @DisplayName("POST /api/accounts responde 200 si la cuenta ya existia")
  void shouldReturn200WhenAlreadyExists() throws Exception {
    when(createAccountUseCase.execute(any())).thenReturn(new CreateAccountResult(account, false));

    mockMvc
        .perform(
            post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("Body invalido responde 400")
  void shouldReturn400WhenBodyIsInvalid() throws Exception {
    mockMvc
        .perform(
            post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"\",\"userName\":\"Ada\",\"userEmail\":\"no-es-email\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("GET /api/accounts lista y GET /api/accounts/user/{id} responde 404 si no existe")
  void shouldListAndReturn404() throws Exception {
    when(getAccountsUseCase.findAll()).thenReturn(List.of(account));
    when(getAccountsUseCase.findByUserId("x"))
        .thenThrow(AccountNotFoundException.becauseUserHasNoAccount("x"));

    mockMvc
        .perform(get("/api/accounts"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].userId").value("u-01"));
    mockMvc
        .perform(get("/api/accounts/user/x"))
        .andExpect(status().isNotFound());
  }
}
