package com.jcaa.usersmanagement.infrastructure.adapter.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import java.time.Duration;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Cache distribuida en Valkey (Aiven, Guia 10). Todas las replicas de users leen las mismas claves,
 * a diferencia de una cache local en RAM.
 *
 * <ul>
 *   <li>Clave: {@code users:user:{id}} (los ":" se ven como carpetas en Redis Insight).
 *   <li>SET con TTL en una sola operacion atomica (SET key value EX ttl): la clave expira sola.
 *   <li>Si Valkey falla, se registra y se sigue con la base de datos (la cache nunca tumba el
 *       servicio).
 * </ul>
 */
@Slf4j
public class ValkeyUserCacheStore implements UserCacheStore {

  static final String KEY_PREFIX = "users:user:";

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;
  private final Duration ttl;

  public ValkeyUserCacheStore(
      final StringRedisTemplate redisTemplate, final ObjectMapper objectMapper, final Duration ttl) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
    this.ttl = ttl;
  }

  @Override
  public Optional<UserModel> find(final UserId userId) {
    try {
      final String json = redisTemplate.opsForValue().get(key(userId));
      if (json == null) {
        log.info("[ValkeyUserCacheStore] MISS {} (se consulta TiDB)", userId.value());
        return Optional.empty();
      }
      log.info("[ValkeyUserCacheStore] HIT {}", userId.value());
      return Optional.of(objectMapper.readValue(json, CachedUser.class).toModel());
    } catch (final JsonProcessingException | RuntimeException exception) {
      log.warn("[ValkeyUserCacheStore] lectura fallida, se usa la base de datos: {}",
          exception.getMessage());
      return Optional.empty();
    }
  }

  @Override
  public void put(final UserModel user) {
    try {
      redisTemplate
          .opsForValue()
          .set(key(user.getId()), objectMapper.writeValueAsString(CachedUser.fromModel(user)), ttl);
    } catch (final JsonProcessingException | RuntimeException exception) {
      log.warn("[ValkeyUserCacheStore] no se pudo guardar en cache: {}", exception.getMessage());
    }
  }

  @Override
  public void evict(final UserId userId) {
    try {
      redisTemplate.delete(key(userId));
    } catch (final RuntimeException exception) {
      log.warn("[ValkeyUserCacheStore] no se pudo invalidar la cache: {}", exception.getMessage());
    }
  }

  private static String key(final UserId userId) {
    return KEY_PREFIX + userId.value();
  }
}
