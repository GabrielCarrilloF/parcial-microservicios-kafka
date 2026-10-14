package com.parcial.accounts.domain.exception;

/** Base de las excepciones de negocio (mismo estilo que el repositorio users del profe). */
public abstract class DomainException extends RuntimeException {

  protected DomainException(final String message) {
    super(message);
  }

  protected DomainException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
