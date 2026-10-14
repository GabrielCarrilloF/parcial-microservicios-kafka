package com.parcial.accounts.infrastructure.entrypoint.rest.mapper;

import com.parcial.accounts.application.dto.CreateAccountCommand;
import com.parcial.accounts.domain.model.AccountModel;
import com.parcial.accounts.infrastructure.entrypoint.rest.dto.AccountRestResponse;
import com.parcial.accounts.infrastructure.entrypoint.rest.dto.CreateAccountRestRequest;

/** Traduce entre el mundo HTTP (DTO) y la capa de aplicacion/dominio. */
public final class AccountRestMapper {

  private AccountRestMapper() {}

  public static CreateAccountCommand toCommand(final CreateAccountRestRequest request) {
    return new CreateAccountCommand(request.userId(), request.userName(), request.userEmail());
  }

  public static AccountRestResponse toResponse(final AccountModel account) {
    return new AccountRestResponse(
        account.getId(),
        account.getUserId(),
        account.getAccountNumber(),
        account.getOwnerName(),
        account.getOwnerEmail(),
        account.getBalance(),
        account.getStatus().name(),
        account.getCreatedAt().toString());
  }
}
