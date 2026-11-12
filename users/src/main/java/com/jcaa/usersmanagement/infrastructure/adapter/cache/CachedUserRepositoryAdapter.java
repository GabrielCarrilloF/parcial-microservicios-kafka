package com.jcaa.usersmanagement.infrastructure.adapter.cache;

import com.jcaa.usersmanagement.application.port.out.DeleteUserPort;
import com.jcaa.usersmanagement.application.port.out.GetUserByIdPort;
import com.jcaa.usersmanagement.application.port.out.UpdateUserPort;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.infrastructure.adapter.persistence.repository.UserRepositoryMySQL;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Patron cache-aside como decorador de los puertos de salida: los casos de uso no cambian.
 *
 * <ul>
 *   <li>Lectura por id: primero Valkey; si no esta (MISS), TiDB y se guarda con TTL.
 *   <li>Actualizar o eliminar: primero TiDB y luego se BORRA la clave (no se reescribe). Asi se
 *       evita la condicion de carrera en la que una replica escribe en cache un dato viejo; la
 *       siguiente lectura trae el dato vigente.
 * </ul>
 */
@Primary
@Component
@RequiredArgsConstructor
public class CachedUserRepositoryAdapter implements GetUserByIdPort, UpdateUserPort, DeleteUserPort {

  private final UserRepositoryMySQL repository;
  private final UserCacheStore cache;

  @Override
  public Optional<UserModel> getById(final UserId userId) {
    final Optional<UserModel> cached = cache.find(userId);
    if (cached.isPresent()) {
      return cached;
    }
    final Optional<UserModel> fromDatabase = repository.getById(userId);
    fromDatabase.ifPresent(cache::put);
    return fromDatabase;
  }

  @Override
  public UserModel update(final UserModel user) {
    final UserModel updated = repository.update(user);
    cache.evict(user.getId());
    return updated;
  }

  @Override
  public void delete(final UserId userId) {
    repository.delete(userId);
    cache.evict(userId);
  }
}
