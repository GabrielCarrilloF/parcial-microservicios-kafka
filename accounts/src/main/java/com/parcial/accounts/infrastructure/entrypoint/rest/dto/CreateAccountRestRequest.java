package com.parcial.accounts.infrastructure.entrypoint.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * JSON que envia users en el paso 5.
 *
 * <p>ACUERDO DEL GRUPO: los nombres deben coincidir con CreateAccountRequest de users.
 */
public record CreateAccountRestRequest(
    @Schema(example = "8f684dc4-0c67-4813-8d58-7b5e7f7937b8") @NotBlank @Size(max = 36)
        String userId,
    @Schema(example = "Ada Lovelace") @NotBlank @Size(max = 100) String userName,
    @Schema(example = "ada@example.com") @NotBlank @Email @Size(max = 150) String userEmail) {}
