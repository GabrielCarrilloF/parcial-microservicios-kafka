package com.jcaa.usersmanagement.infrastructure.adapter.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.enums.UserStatus;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserEmail;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.domain.valueobject.UserName;
import com.jcaa.usersmanagement.domain.valueobject.UserPassword;
import com.jcaa.usersmanagement.infrastructure.adapter.persistence.repository.UserRepositoryMySQL;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CachedUserRepositoryAdapterTest {

  private static final UserId ID = new UserId("u-1");
  private static final UserModel USER =
      new UserModel(
          ID,
          new UserName("Ana Torres"),
          new UserEmail("ana@mail.com"),
          UserPassword.fromHash("$2a$12$hash"),
          UserRole.MEMBER,
          UserStatus.ACTIVE);

  @Mock private UserRepositoryMySQL repository;
  @Mock private UserCacheStore cache;

  private CachedUserRepositoryAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new CachedUserRepositoryAdapter(repository, cache);
  }

  @Test
  void hitDoesNotQueryTheDatabase() {
    when(cache.find(ID)).thenReturn(Optional.of(USER));

    assertThat(adapter.getById(ID)).contains(USER);
    verify(repository, never()).getById(ID);
  }

  @Test
  void missQueriesDatabaseAndStoresInCache() {
    when(cache.find(ID)).thenReturn(Optional.empty());
    when(repository.getById(ID)).thenReturn(Optional.of(USER));

    assertThat(adapter.getById(ID)).contains(USER);
    verify(cache).put(USER);
  }

  @Test
  void updateWritesDatabaseThenEvictsKey() {
    when(repository.update(USER)).thenReturn(USER);

    adapter.update(USER);

    final InOrder order = inOrder(repository, cache);
    order.verify(repository).update(USER);
    order.verify(cache).evict(ID);
  }

  @Test
  void deleteEvictsKey() {
    adapter.delete(ID);

    verify(repository).delete(ID);
    verify(cache).evict(ID);
  }
}
