package com.parcial.accounts.application.port.in;

import com.parcial.accounts.domain.model.AccountModel;
import java.util.List;

/** Puerto de entrada: consultas de cuentas (para verificar el paso 6 en la sustentacion). */
public interface GetAccountsUseCase {

  List<AccountModel> findAll();

  AccountModel findByUserId(String userId);
}
