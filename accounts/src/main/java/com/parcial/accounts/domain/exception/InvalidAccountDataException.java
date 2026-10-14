package com.parcial.accounts.domain.exception;

/** Datos obligatorios de la cuenta vacios o con formato invalido. */
public final class InvalidAccountDataException extends DomainException {

  private InvalidAccountDataException(final String message) {
    super(message);
  }

  public static InvalidAccountDataException becauseFieldIsRequired(final String field) {
    return new InvalidAccountDataException("El campo '" + field + "' de la cuenta es obligatorio.");
  }
}
