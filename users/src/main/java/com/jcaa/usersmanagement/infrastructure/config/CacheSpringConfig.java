package com.jcaa.usersmanagement.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcaa.usersmanagement.infrastructure.adapter.cache.DisabledUserCacheStore;
import com.jcaa.usersmanagement.infrastructure.adapter.cache.UserCacheStore;
import com.jcaa.usersmanagement.infrastructure.adapter.cache.ValkeyUserCacheStore;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Activa la cache de Valkey solo si VALKEY_URL esta definida (Service URI de Aiven:
 * rediss://default:CLAVE@HOST:PUERTO). Sin ella el servicio funciona igual, sin cache.
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class CacheSpringConfig {

  @Bean
  public UserCacheStore userCacheStore(
      @Value("${cache.valkey-url:}") final String valkeyUrl,
      @Value("${cache.users.ttl:10m}") final Duration ttl,
      final ObjectProvider<StringRedisTemplate> redisTemplate,
      final ObjectMapper objectMapper) {
    if (valkeyUrl.isBlank()) {
      log.warn("[CacheSpringConfig] VALKEY_URL no definida: cache distribuida deshabilitada.");
      return new DisabledUserCacheStore();
    }
    log.info("[CacheSpringConfig] cache distribuida Valkey activa (TTL {}).", ttl);
    return new ValkeyUserCacheStore(redisTemplate.getObject(), objectMapper, ttl);
  }
}
