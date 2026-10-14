package com.parcial.accounts.application.port.in;

import com.parcial.accounts.application.dto.CreateAccountCommand;
import com.parcial.accounts.application.dto.CreateAccountResult;

/** Puerto de entrada: crear la cuenta de un usuario nuevo (pasos 5, 6 y 7). */
public interface CreateAccountUseCase {

  CreateAccountResult execute(CreateAccountCommand command);
}
