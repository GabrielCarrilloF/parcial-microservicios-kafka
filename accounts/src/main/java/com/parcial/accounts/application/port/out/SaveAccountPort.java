package com.parcial.accounts.application.port.out;

import com.parcial.accounts.domain.model.AccountModel;

/** Puerto de salida del paso 6: guardar la cuenta en TiDB. */
public interface SaveAccountPort {

  AccountModel save(AccountModel account);
}
