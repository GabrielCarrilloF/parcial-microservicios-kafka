package com.parcial.accounts.infrastructure.entrypoint.rest.controller;

import com.parcial.accounts.application.dto.CreateAccountResult;
import com.parcial.accounts.application.port.in.CreateAccountUseCase;
import com.parcial.accounts.application.port.in.GetAccountsUseCase;
import com.parcial.accounts.infrastructure.entrypoint.rest.dto.AccountRestResponse;
import com.parcial.accounts.infrastructure.entrypoint.rest.dto.CreateAccountRestRequest;
import com.parcial.accounts.infrastructure.entrypoint.rest.mapper.AccountRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Punto de entrada del paso 5: users llama a {@code POST /api/accounts}.
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "accounts", description = "Cuentas de los usuarios (pasos 5, 6 y 7)")
public class AccountRestController {

  private final CreateAccountUseCase createAccountUseCase;
  private final GetAccountsUseCase getAccountsUseCase;

  @PostMapping
  @Operation(summary = "Crea la cuenta de un usuario (201) o devuelve la existente (200)")
  public ResponseEntity<AccountRestResponse> create(
      @Valid @RequestBody final CreateAccountRestRequest request) {
    final CreateAccountResult result =
        createAccountUseCase.execute(AccountRestMapper.toCommand(request));
    final HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
    return ResponseEntity.status(status).body(AccountRestMapper.toResponse(result.account()));
  }

  @GetMapping
  @Operation(summary = "Lista todas las cuentas (evidencia del paso 6)")
  public List<AccountRestResponse> findAll() {
    return getAccountsUseCase.findAll().stream().map(AccountRestMapper::toResponse).toList();
  }

  @GetMapping("/user/{userId}")
  @Operation(summary = "Consulta la cuenta de un usuario")
  public AccountRestResponse findByUserId(@PathVariable final String userId) {
    return AccountRestMapper.toResponse(getAccountsUseCase.findByUserId(userId));
  }
}
