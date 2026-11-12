package com.jcaa.usersmanagement.infrastructure.adapter.cache;

import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.enums.UserStatus;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserEmail;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.domain.valueobject.UserName;
import com.jcaa.usersmanagement.domain.valueobject.UserPassword;

/**
 * Valor JSON guardado en Valkey. La contrasena solo viaja como hash BCrypt (nunca en texto plano)
 * y la conexion con Aiven es cifrada (rediss://, TLS).
 */
record CachedUser(
    String id, String name, String email, String passwordHash, String role, String status) {

  static CachedUser fromModel(final UserModel user) {
    return new CachedUser(
        user.getId().value(),
        user.getName().value(),
        user.getEmail().value(),
        user.getPassword().value(),
        user.getRole().name(),
        user.getStatus().name());
  }

  UserModel toModel() {
    return new UserModel(
        new UserId(id),
        new UserName(name),
        new UserEmail(email),
        UserPassword.fromHash(passwordHash),
        UserRole.fromString(role),
        UserStatus.fromString(status));
  }
}
