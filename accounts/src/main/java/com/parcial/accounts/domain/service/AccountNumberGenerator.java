package com.parcial.accounts.domain.service;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

/**
 * Genera numeros de cuenta con formato {@code AC-XXXXXXXXXX} (10 digitos aleatorios).
 *
 * <p>La columna account_number es UNIQUE en TiDB, asi que una colision (muy improbable) seria
 * rechazada por la base de datos y no se guardaria una cuenta duplicada.
 */
@Component
public class AccountNumberGenerator {

  private static final String PREFIX = "AC-";
  private static final int DIGITS = 10;
  private final SecureRandom random = new SecureRandom();

  public String next() {
    final StringBuilder number = new StringBuilder(PREFIX);
    for (int i = 0; i < DIGITS; i++) {
      number.append(random.nextInt(10));
    }
    return number.toString();
  }
}
