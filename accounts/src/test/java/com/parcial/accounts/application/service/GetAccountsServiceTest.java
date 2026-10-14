package com.parcial.accounts.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.parcial.accounts.application.port.out.FindAccountsPort;
import com.parcial.accounts.domain.exception.AccountNotFoundException;
import com.parcial.accounts.domain.model.AccountModel;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GetAccountsService")
class GetAccountsServiceTest {

  private final FindAccountsPort findAccountsPort = mock(FindAccountsPort.class);
  private final GetAccountsService service = new GetAccountsService(findAccountsPort);

  @Test
  @DisplayName("findAll() y findByUserId() delegan en el puerto")
  void shouldDelegateToPort() {
    final AccountModel account = AccountModel.openFor("u-01", "Ada", "ada@example.com", "AC-1");
    when(findAccountsPort.findAll()).thenReturn(List.of(account));
    when(findAccountsPort.findByUserId("u-01")).thenReturn(Optional.of(account));

    assertEquals(1, service.findAll().size());
    assertEquals("AC-1", service.findByUserId("u-01").getAccountNumber());
  }

  @Test
  @DisplayName("findByUserId() lanza AccountNotFoundException si no hay cuenta")
  void shouldThrowWhenNotFound() {
    when(findAccountsPort.findByUserId("x")).thenReturn(Optional.empty());

    assertThrows(AccountNotFoundException.class, () -> service.findByUserId("x"));
  }
}
