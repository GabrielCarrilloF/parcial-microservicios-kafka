package com.parcial.accounts.domain.exception;

/** No existe cuenta para el usuario consultado. */
public final class AccountNotFoundException extends DomainException {

  private AccountNotFoundException(final String message) {
    super(message);
  }

  public static AccountNotFoundException becauseUserHasNoAccount(final String userId) {
    return new AccountNotFoundException("El usuario '" + userId + "' no tiene cuenta.");
  }
}
