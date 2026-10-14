package com.parcial.accounts.infrastructure.entrypoint.rest.advice;

import com.parcial.accounts.domain.exception.AccountNotFoundException;
import com.parcial.accounts.domain.exception.InvalidAccountDataException;
import com.parcial.accounts.infrastructure.entrypoint.rest.dto.ApiErrorResponse;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Convierte excepciones en respuestas JSON con el codigo HTTP correcto. */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(AccountNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ApiErrorResponse handleNotFound(final AccountNotFoundException exception) {
    return new ApiErrorResponse(HttpStatus.NOT_FOUND.value(), exception.getMessage());
  }

  @ExceptionHandler(InvalidAccountDataException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiErrorResponse handleInvalidData(final InvalidAccountDataException exception) {
    return new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), exception.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiErrorResponse handleValidation(final MethodArgumentNotValidException exception) {
    final String message =
        exception.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
    return new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiErrorResponse handleUnreadable(final HttpMessageNotReadableException exception) {
    return new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), "El cuerpo JSON es invalido.");
  }

  /** Dos peticiones simultaneas para el mismo usuario: la segunda choca con user_id UNIQUE. */
  @ExceptionHandler(DuplicateKeyException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiErrorResponse handleDuplicate(final DuplicateKeyException exception) {
    return new ApiErrorResponse(
        HttpStatus.CONFLICT.value(), "El usuario ya tiene una cuenta registrada.");
  }

  @ExceptionHandler(DataAccessException.class)
  @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
  public ApiErrorResponse handleDatabase(final DataAccessException exception) {
    log.error("[GlobalExceptionHandler] error de base de datos", exception);
    return new ApiErrorResponse(
        HttpStatus.SERVICE_UNAVAILABLE.value(), "La base de datos no esta disponible.");
  }
}
