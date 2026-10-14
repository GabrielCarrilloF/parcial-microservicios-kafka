package com.parcial.notifiers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio notifiers.
 *
 * <ul>
 *   <li>Paso 3: consume la cola notification.events.queue de RabbitMQ (eventos user.created,
 *       user.updated y account.created).
 *   <li>Paso 4: envia el correo con la API REST de Brevo (codigo migrado desde users).
 * </ul>
 */
@SpringBootApplication
public class NotifiersApplication {

  public static void main(final String[] args) {
    SpringApplication.run(NotifiersApplication.class, args);
  }
}
