package com.parcial.accounts.application.port.out;

import com.parcial.accounts.domain.model.AccountModel;
import java.util.List;
import java.util.Optional;

/** Puerto de salida para leer cuentas desde TiDB. */
public interface FindAccountsPort {

  Optional<AccountModel> findByUserId(String userId);

  List<AccountModel> findAll();
}
