package com.parcial.accounts.infrastructure.adapter.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.parcial.accounts.domain.enums.AccountStatus;
import com.parcial.accounts.domain.model.AccountModel;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@DisplayName("AccountJdbcRepository (paso 6: accounts -> TiDB)")
class AccountJdbcRepositoryTest {

  private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
  private final AccountJdbcRepository repository = new AccountJdbcRepository(jdbcTemplate);

  @Test
  @DisplayName("save() ejecuta el INSERT con todos los campos")
  void shouldInsertAccount() {
    final AccountModel account = AccountModel.openFor("u-01", "Ada", "ada@example.com", "AC-1");

    final AccountModel saved = repository.save(account);

    assertSame(account, saved);
    verify(jdbcTemplate)
        .update(
            eq(AccountJdbcRepository.INSERT_SQL),
            eq(account.getId()),
            eq("u-01"),
            eq("AC-1"),
            eq("Ada"),
            eq("ada@example.com"),
            eq(BigDecimal.ZERO),
            eq("ACTIVE"),
            any(Timestamp.class));
  }

  @Test
  @DisplayName("findByUserId() devuelve vacio si no hay filas")
  @SuppressWarnings("unchecked")
  void shouldReturnEmptyWhenNoRows() {
    when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq("u-404"))).thenReturn(List.of());

    assertTrue(repository.findByUserId("u-404").isEmpty());
  }

  @Test
  @DisplayName("mapRow() convierte una fila de TiDB en AccountModel")
  void shouldMapRow() throws Exception {
    final ResultSet rs = mock(ResultSet.class);
    when(rs.getString("id")).thenReturn("id-1");
    when(rs.getString("user_id")).thenReturn("u-01");
    when(rs.getString("account_number")).thenReturn("AC-1");
    when(rs.getString("owner_name")).thenReturn("Ada");
    when(rs.getString("owner_email")).thenReturn("ada@example.com");
    when(rs.getBigDecimal("balance")).thenReturn(new BigDecimal("10.50"));
    when(rs.getString("status")).thenReturn("BLOCKED");
    when(rs.getTimestamp("created_at"))
        .thenReturn(Timestamp.valueOf(LocalDateTime.of(2026, 10, 13, 10, 0)));

    final AccountModel account = AccountJdbcRepository.mapRow(rs, 0);

    assertEquals("u-01", account.getUserId());
    assertEquals(AccountStatus.BLOCKED, account.getStatus());
    assertEquals(new BigDecimal("10.50"), account.getBalance());
  }
}
