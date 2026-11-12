package com.jcaa.usersmanagement.infrastructure.adapter.cache;

import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import java.util.Optional;

/** Almacen de cache de usuarios (Valkey en la nube o deshabilitado). */
public interface UserCacheStore {

  Optional<UserModel> find(UserId userId);

  void put(UserModel user);

  void evict(UserId userId);
}
