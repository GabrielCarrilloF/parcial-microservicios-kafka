package com.parcial.accounts.application.service;

import com.parcial.accounts.application.dto.CreateAccountCommand;
import com.parcial.accounts.application.dto.CreateAccountResult;
import com.parcial.accounts.application.port.in.CreateAccountUseCase;
import com.parcial.accounts.application.port.out.AccountEventPublisherPort;
import com.parcial.accounts.application.port.out.FindAccountsPort;
import com.parcial.accounts.application.port.out.SaveAccountPort;
import com.parcial.accounts.domain.model.AccountModel;
import com.parcial.accounts.domain.service.AccountNumberGenerator;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Orquesta los pasos 6 y 7 del diagrama:
 *
 * <ol>
 *   <li>Si el usuario ya tiene cuenta, la devuelve (idempotente, users puede reintentar).
 *   <li>Paso 6: abre la cuenta y la guarda en TiDB.
 *   <li>Paso 7: publica el evento account.created en RabbitMQ.
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateAccountService implements CreateAccountUseCase {

  private final FindAccountsPort findAccountsPort;
  private final SaveAccountPort saveAccountPort;
  private final AccountEventPublisherPort accountEventPublisherPort;
  private final AccountNumberGenerator accountNumberGenerator;

  @Override
  public CreateAccountResult execute(final CreateAccountCommand command) {
    final Optional<AccountModel> existing = findAccountsPort.findByUserId(command.userId());
    if (existing.isPresent()) {
      log.info(
          "[CreateAccountService] el usuario {} ya tenia la cuenta {}.",
          command.userId(),
          existing.get().getAccountNumber());
      return new CreateAccountResult(existing.get(), false);
    }

    final AccountModel newAccount =
        AccountModel.openFor(
            command.userId(),
            command.userName(),
            command.userEmail(),
            accountNumberGenerator.next());

    // Paso 6
    final AccountModel saved = saveAccountPort.save(newAccount);
    log.info(
        "[CreateAccountService] cuenta {} creada para el usuario {}.",
        saved.getAccountNumber(),
        saved.getUserId());

    // Paso 7
    accountEventPublisherPort.publishAccountCreated(saved);
    return new CreateAccountResult(saved, true);
  }
}
