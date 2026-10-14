package com.parcial.accounts.infrastructure.entrypoint.rest.dto;

import java.math.BigDecimal;

/** Respuesta JSON de una cuenta. */
public record AccountRestResponse(
    String id,
    String userId,
    String accountNumber,
    String ownerName,
    String ownerEmail,
    BigDecimal balance,
    String status,
    String createdAt) {}
