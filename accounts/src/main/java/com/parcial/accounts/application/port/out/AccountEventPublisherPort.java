package com.parcial.accounts.application.port.out;

import com.parcial.accounts.domain.model.AccountModel;

/**
 * Puerto de salida del paso 7 del diagrama: publicar el evento account.created para que el
 * microservicio notifiers envie el correo de cuenta creada.
 */
public interface AccountEventPublisherPort {

  void publishAccountCreated(AccountModel account);
}
