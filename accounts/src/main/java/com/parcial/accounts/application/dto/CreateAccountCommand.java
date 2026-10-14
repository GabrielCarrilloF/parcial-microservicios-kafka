package com.parcial.accounts.application.dto;

/** Datos que llegan de users para abrir la cuenta (paso 5). */
public record CreateAccountCommand(String userId, String userName, String userEmail) {}
