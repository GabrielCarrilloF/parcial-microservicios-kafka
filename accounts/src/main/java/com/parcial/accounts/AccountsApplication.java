package com.parcial.accounts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio accounts (Integrante 2).
 *
 * <p>Responsabilidad en el diagrama del parcial:
 *
 * <ul>
 *   <li>Paso 5: recibe de users la solicitud sincrona "crear cuenta" (POST /api/accounts).
 *   <li>Paso 6: guarda la cuenta en TiDB (tabla accounts).
 *   <li>Paso 7: publica el evento account.created en RabbitMQ para que notifiers envie el correo.
 * </ul>
 */
@SpringBootApplication
public class AccountsApplication {

  public static void main(final String[] args) {
    SpringApplication.run(AccountsApplication.class, args);
  }
}
