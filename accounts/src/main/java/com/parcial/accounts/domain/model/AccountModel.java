package com.parcial.accounts.domain.model;

import com.parcial.accounts.domain.enums.AccountStatus;
import com.parcial.accounts.domain.exception.InvalidAccountDataException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Value;

/**
 * Entidad de dominio "cuenta". Cada usuario de users tiene como maximo UNA cuenta (user_id es
 * UNIQUE en la tabla accounts).
 */
@Value
public class AccountModel {

  String id;
  String userId;
  String accountNumber;
  String ownerName;
  String ownerEmail;
  BigDecimal balance;
  AccountStatus status;
  LocalDateTime createdAt;

  public AccountModel(
      final String id,
      final String userId,
      final String accountNumber,
      final String ownerName,
      final String ownerEmail,
      final BigDecimal balance,
      final AccountStatus status,
      final LocalDateTime createdAt) {
    this.id = requireText(id, "id");
    this.userId = requireText(userId, "userId");
    this.accountNumber = requireText(accountNumber, "accountNumber");
    this.ownerName = requireText(ownerName, "ownerName");
    this.ownerEmail = requireText(ownerEmail, "ownerEmail").trim().toLowerCase();
    this.balance = balance == null ? BigDecimal.ZERO : balance;
    this.status = status == null ? AccountStatus.ACTIVE : status;
    this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
  }

  /** Fabrica para abrir la cuenta de un usuario nuevo: saldo 0 y estado ACTIVE. */
  public static AccountModel openFor(
      final String userId,
      final String ownerName,
      final String ownerEmail,
      final String accountNumber) {
    return new AccountModel(
        UUID.randomUUID().toString(),
        userId,
        accountNumber,
        ownerName,
        ownerEmail,
        BigDecimal.ZERO,
        AccountStatus.ACTIVE,
        LocalDateTime.now());
  }

  private static String requireText(final String value, final String field) {
    if (value == null || value.isBlank()) {
      throw InvalidAccountDataException.becauseFieldIsRequired(field);
    }
    return value;
  }
}
