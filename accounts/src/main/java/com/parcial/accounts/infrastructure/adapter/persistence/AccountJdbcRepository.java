package com.parcial.accounts.infrastructure.adapter.persistence;

import com.parcial.accounts.application.port.out.FindAccountsPort;
import com.parcial.accounts.application.port.out.SaveAccountPort;
import com.parcial.accounts.domain.enums.AccountStatus;
import com.parcial.accounts.domain.model.AccountModel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * Paso 6 del diagrama: adaptador de persistencia sobre TiDB (MySQL compatible) con JdbcTemplate.
 *
 * <p>Usa la misma base de datos que users ({@code crud_usuarios}) pero su propia tabla
 * {@code accounts} (ver src/main/resources/schema.sql).
 */
@Repository
@RequiredArgsConstructor
public class AccountJdbcRepository implements SaveAccountPort, FindAccountsPort {

  static final String INSERT_SQL =
      "INSERT INTO accounts (id, user_id, account_number, owner_name, owner_email, balance,"
          + " status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
  static final String SELECT_COLUMNS =
      "SELECT id, user_id, account_number, owner_name, owner_email, balance, status, created_at"
          + " FROM accounts";
  static final String FIND_BY_USER_SQL = SELECT_COLUMNS + " WHERE user_id = ?";
  static final String FIND_ALL_SQL = SELECT_COLUMNS + " ORDER BY created_at DESC";

  private static final RowMapper<AccountModel> ROW_MAPPER = AccountJdbcRepository::mapRow;

  private final JdbcTemplate jdbcTemplate;

  @Override
  public AccountModel save(final AccountModel account) {
    jdbcTemplate.update(
        INSERT_SQL,
        account.getId(),
        account.getUserId(),
        account.getAccountNumber(),
        account.getOwnerName(),
        account.getOwnerEmail(),
        account.getBalance(),
        account.getStatus().name(),
        Timestamp.valueOf(account.getCreatedAt()));
    return account;
  }

  @Override
  public Optional<AccountModel> findByUserId(final String userId) {
    return jdbcTemplate.query(FIND_BY_USER_SQL, ROW_MAPPER, userId).stream().findFirst();
  }

  @Override
  public List<AccountModel> findAll() {
    return jdbcTemplate.query(FIND_ALL_SQL, ROW_MAPPER);
  }

  static AccountModel mapRow(final ResultSet rs, final int rowNum) throws SQLException {
    final Timestamp createdAt = rs.getTimestamp("created_at");
    return new AccountModel(
        rs.getString("id"),
        rs.getString("user_id"),
        rs.getString("account_number"),
        rs.getString("owner_name"),
        rs.getString("owner_email"),
        rs.getBigDecimal("balance"),
        AccountStatus.valueOf(rs.getString("status")),
        createdAt == null ? null : createdAt.toLocalDateTime());
  }
}
