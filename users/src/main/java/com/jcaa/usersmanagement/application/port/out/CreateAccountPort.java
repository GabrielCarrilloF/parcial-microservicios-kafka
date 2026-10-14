package com.jcaa.usersmanagement.application.port.out;

import com.jcaa.usersmanagement.domain.model.UserModel;

/**
 * Puerto de salida del paso 5 del diagrama: users le pide (sincronamente) al microservicio
 * accounts que cree la cuenta del usuario recien registrado.
 *
 * <p>La capa de aplicacion solo conoce este contrato; el detalle HTTP vive en el adaptador
 * {@code AccountsRestClientAdapter} (infraestructura).
 */
public interface CreateAccountPort {

  void createAccountFor(UserModel user);
}
