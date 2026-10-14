package com.parcial.accounts.application.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.parcial.accounts.application.dto.CreateAccountCommand;
import com.parcial.accounts.application.dto.CreateAccountResult;
import com.parcial.accounts.application.port.out.AccountEventPublisherPort;
import com.parcial.accounts.application.port.out.FindAccountsPort;
import com.parcial.accounts.application.port.out.SaveAccountPort;
import com.parcial.accounts.domain.enums.AccountStatus;
import com.parcial.accounts.domain.model.AccountModel;
import com.parcial.accounts.domain.service.AccountNumberGenerator;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("CreateAccountService (pasos 6 y 7)")
@ExtendWith(MockitoExtension.class)
class CreateAccountServiceTest {

  @Mock private FindAccountsPort findAccountsPort;
  @Mock private SaveAccountPort saveAccountPort;
  @Mock private AccountEventPublisherPort accountEventPublisherPort;
  @Mock private AccountNumberGenerator accountNumberGenerator;

  private CreateAccountService service;
  private final CreateAccountCommand command =
      new CreateAccountCommand("u-01", "Ada Lovelace", "ADA@example.com");

  @BeforeEach
  void setUp() {
    service =
        new CreateAccountService(
            findAccountsPort, saveAccountPort, accountEventPublisherPort, accountNumberGenerator);
  }

  @Test
  @DisplayName("Crea, guarda y notifica la cuenta de un usuario nuevo")
  void shouldCreateSaveAndNotify() {
    when(findAccountsPort.findByUserId("u-01")).thenReturn(Optional.empty());
    when(accountNumberGenerator.next()).thenReturn("AC-0000000001");
    when(saveAccountPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    final CreateAccountResult result = service.execute(command);

    final AccountModel account = result.account();
    assertAll(
        () -> assertTrue(result.created()),
        () -> assertEquals("u-01", account.getUserId()),
        () -> assertEquals("AC-0000000001", account.getAccountNumber()),
        () -> assertEquals("ada@example.com", account.getOwnerEmail()),
        () -> assertEquals(BigDecimal.ZERO, account.getBalance()),
        () -> assertEquals(AccountStatus.ACTIVE, account.getStatus()));
    verify(accountEventPublisherPort).publishAccountCreated(account);
  }

  @Test
  @DisplayName("Si el usuario ya tiene cuenta la devuelve sin crear otra (idempotente)")
  void shouldReturnExistingAccount() {
    final AccountModel existing = AccountModel.openFor("u-01", "Ada", "ada@example.com", "AC-1");
    when(findAccountsPort.findByUserId("u-01")).thenReturn(Optional.of(existing));

    final CreateAccountResult result = service.execute(command);

    assertFalse(result.created());
    assertSame(existing, result.account());
    verify(saveAccountPort, never()).save(any());
    verifyNoInteractions(accountEventPublisherPort, accountNumberGenerator);
  }
}
