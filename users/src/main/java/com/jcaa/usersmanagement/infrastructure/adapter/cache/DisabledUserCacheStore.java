package com.jcaa.usersmanagement.infrastructure.adapter.cache;

import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import java.util.Optional;

/** Se usa cuando VALKEY_URL no esta definida (por ejemplo, en local sin Aiven). */
public class DisabledUserCacheStore implements UserCacheStore {

  @Override
  public Optional<UserModel> find(final UserId userId) {
    return Optional.empty();
  }

  @Override
  public void put(final UserModel user) {
    // Sin cache: no hace nada.
  }

  @Override
  public void evict(final UserId userId) {
    // Sin cache: no hace nada.
  }
}
