package com.jcaa.usersmanagement.infrastructure.adapter.accounts;

/**
 * Cuerpo JSON que users envia a {@code POST /api/accounts} del microservicio accounts.
 *
 * <p>ACUERDO DEL GRUPO: los nombres de los campos deben coincidir con CreateAccountRestRequest de
 * accounts (userId, userName, userEmail).
 */
public record CreateAccountRequest(String userId, String userName, String userEmail) {}
