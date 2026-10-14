package com.jcaa.usersmanagement.domain.exception;

/**
 * Se lanza cuando el microservicio accounts no pudo crear la cuenta del usuario (paso 5).
 *
 * <p>El usuario ya quedo guardado en TiDB, por eso CreateUserService registra el error en el log
 * pero no revierte la creacion del usuario.
 */
public final class AccountCreationException extends DomainException {

  private static final String MESSAGE =
      "No se pudo crear la cuenta del usuario '%s' en el microservicio accounts: %s";

  private AccountCreationException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public static AccountCreationException becauseAccountsServiceFailed(
      final String userId, final String detail, final Throwable cause) {
    return new AccountCreationException(String.format(MESSAGE, userId, detail), cause);
  }
}
