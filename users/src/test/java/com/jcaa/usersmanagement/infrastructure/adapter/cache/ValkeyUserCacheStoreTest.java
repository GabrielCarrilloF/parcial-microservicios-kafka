package com.jcaa.usersmanagement.infrastructure.adapter.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.enums.UserStatus;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserEmail;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.domain.valueobject.UserName;
import com.jcaa.usersmanagement.domain.valueobject.UserPassword;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class ValkeyUserCacheStoreTest {

  private static final Duration TTL = Duration.ofMinutes(10);
  private static final UserModel USER =
      new UserModel(
          new UserId("u-1"),
          new UserName("Ana Torres"),
          new UserEmail("ana@mail.com"),
          UserPassword.fromHash("$2a$12$hash"),
          UserRole.MEMBER,
          UserStatus.ACTIVE);

  @Mock private StringRedisTemplate redisTemplate;
  @Mock private ValueOperations<String, String> valueOperations;

  private ValkeyUserCacheStore store;

  @BeforeEach
  void setUp() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    store = new ValkeyUserCacheStore(redisTemplate, new ObjectMapper(), TTL);
  }

  @Test
  void putStoresJsonWithTtlAndFindReadsItBack() {
    final ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);

    store.put(USER);

    verify(valueOperations).set(eq("users:user:u-1"), json.capture(), eq(TTL));
    when(valueOperations.get("users:user:u-1")).thenReturn(json.getValue());
    assertThat(store.find(new UserId("u-1"))).contains(USER);
  }

  @Test
  void valkeyDownMeansCacheMiss() {
    when(valueOperations.get(anyString()))
        .thenThrow(new RedisConnectionFailureException("Valkey no disponible"));

    assertThat(store.find(new UserId("u-1"))).isEmpty();
  }
}
