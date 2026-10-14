package com.parcial.accounts.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.parcial.accounts.domain.enums.AccountStatus;
import com.parcial.accounts.domain.exception.InvalidAccountDataException;
import com.parcial.accounts.domain.model.AccountModel;
import com.parcial.accounts.domain.service.AccountNumberGenerator;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Dominio de accounts")
class AccountDomainTest {

  @Test
  @DisplayName("openFor() crea cuenta ACTIVE con saldo 0 y email normalizado")
  void shouldOpenAccountWithDefaults() {
    final AccountModel account =
        AccountModel.openFor("u-01", "Ada", "  ADA@Example.com ", "AC-0000000001");

    assertNotNull(account.getId());
    assertEquals(AccountStatus.ACTIVE, account.getStatus());
    assertEquals(BigDecimal.ZERO, account.getBalance());
    assertEquals("ada@example.com", account.getOwnerEmail());
    assertNotNull(account.getCreatedAt());
  }

  @Test
  @DisplayName("Rechaza campos obligatorios vacios")
  void shouldRejectBlankFields() {
    assertThrows(
        InvalidAccountDataException.class,
        () -> AccountModel.openFor(" ", "Ada", "ada@example.com", "AC-1"));
    assertThrows(
        InvalidAccountDataException.class,
        () -> AccountModel.openFor("u-01", "Ada", null, "AC-1"));
  }

  @Test
  @DisplayName("AccountNumberGenerator genera AC- seguido de 10 digitos")
  void shouldGenerateAccountNumbers() {
    final String number = new AccountNumberGenerator().next();

    assertTrue(number.matches("AC-\\d{10}"), number);
  }
}
