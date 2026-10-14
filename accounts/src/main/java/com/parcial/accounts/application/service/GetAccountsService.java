package com.parcial.accounts.application.service;

import com.parcial.accounts.application.port.in.GetAccountsUseCase;
import com.parcial.accounts.application.port.out.FindAccountsPort;
import com.parcial.accounts.domain.exception.AccountNotFoundException;
import com.parcial.accounts.domain.model.AccountModel;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Consultas de cuentas. */
@Service
@RequiredArgsConstructor
public class GetAccountsService implements GetAccountsUseCase {

  private final FindAccountsPort findAccountsPort;

  @Override
  public List<AccountModel> findAll() {
    return findAccountsPort.findAll();
  }

  @Override
  public AccountModel findByUserId(final String userId) {
    return findAccountsPort
        .findByUserId(userId)
        .orElseThrow(() -> AccountNotFoundException.becauseUserHasNoAccount(userId));
  }
}
