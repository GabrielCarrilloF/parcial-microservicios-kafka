package com.parcial.accounts.application.dto;

import com.parcial.accounts.domain.model.AccountModel;

/**
 * Resultado del caso de uso. {@code created=false} significa que el usuario ya tenia cuenta y se
 * devolvio la existente (idempotencia: si users reintenta, no se crean cuentas duplicadas).
 */
public record CreateAccountResult(AccountModel account, boolean created) {}
