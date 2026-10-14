package com.parcial.accounts.infrastructure.entrypoint.rest.dto;

/** Formato de error igual al del repositorio users del profe. */
public record ApiErrorResponse(int status, String message) {}
