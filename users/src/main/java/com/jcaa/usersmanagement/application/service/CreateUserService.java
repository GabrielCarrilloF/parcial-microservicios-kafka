package com.jcaa.usersmanagement.application.service;

import com.jcaa.usersmanagement.application.port.in.CreateUserUseCase;
import com.jcaa.usersmanagement.application.port.out.CreateAccountPort;
import com.jcaa.usersmanagement.application.port.out.DomainEventPublisherPort;
import com.jcaa.usersmanagement.application.port.out.GetUserByEmailPort;
import com.jcaa.usersmanagement.application.port.out.SaveUserPort;
import com.jcaa.usersmanagement.application.service.dto.command.CreateUserCommand;
import com.jcaa.usersmanagement.application.service.mapper.UserApplicationMapper;
import com.jcaa.usersmanagement.domain.event.UserCreatedDomainEvent;
import com.jcaa.usersmanagement.domain.exception.AccountCreationException;
import com.jcaa.usersmanagement.domain.exception.UserAlreadyExistsException;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserEmail;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import jakarta.validation.ConstraintViolation;

/**
 * Caso de uso "crear usuario" segun el diagrama del parcial:
 *
 * <ol>
 *   <li>Paso 1 (sincrono): guarda el usuario en TiDB.
 *   <li>Paso 2 (asincrono): publica el evento user.created en RabbitMQ.
 *   <li>Paso 5 (sincrono): solicita a accounts la creacion de la cuenta.
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateUserService implements CreateUserUseCase {

  private final SaveUserPort saveUserPort;
  private final GetUserByEmailPort getUserByEmailPort;
  private final DomainEventPublisherPort domainEventPublisherPort;
  private final CreateAccountPort createAccountPort;
  private final Validator validator;

  @Override
  public UserModel execute(final CreateUserCommand command) {
    validateCommand(command);

    final UserEmail email = new UserEmail(command.email());
    ensureEmailIsNotTaken(email);

    final UserModel userToSave = UserApplicationMapper.fromCreateCommandToModel(command);
    final UserModel savedUser = saveUserPort.save(userToSave);

    domainEventPublisherPort.publish(new UserCreatedDomainEvent(savedUser));
    requestAccountCreation(savedUser);

    return savedUser;
  }

  /** Fallback: si accounts no responde, el usuario ya guardado no se revierte. */
  private void requestAccountCreation(final UserModel savedUser) {
    try {
      createAccountPort.createAccountFor(savedUser);
    } catch (final AccountCreationException exception) {
      log.warn("[CreateUserService] {}", exception.getMessage());
    }
  }

  private void validateCommand(final CreateUserCommand command) {
    final Set<ConstraintViolation<CreateUserCommand>> violations = validator.validate(command);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }

  private void ensureEmailIsNotTaken(final UserEmail email) {
    getUserByEmailPort
        .getByEmail(email)
        .ifPresent(
            ignored -> {
              throw UserAlreadyExistsException.becauseEmailAlreadyExists(email.value());
            });
  }
}
