package com.parcial.accounts.infrastructure.adapter.messaging;

import com.parcial.accounts.application.port.out.AccountEventPublisherPort;
import com.parcial.accounts.domain.model.AccountModel;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Paso 7 del diagrama (asincrono): publica el evento {@code account.created} en el exchange
 * {@code domain.events} de RabbitMQ. El binding con routing key account.created lo lleva a la cola
 * notification.events.queue, que consume notifiers (patron FanIn).
 *
 * <p>Fallback: si RabbitMQ no esta disponible, la cuenta ya guardada no se revierte y el error se
 * registra en el log.
 */
@Slf4j
@Component
public class RabbitAccountEventPublisherAdapter implements AccountEventPublisherPort {

  static final String EVENT_TYPE = "account.created";

  private final RabbitTemplate rabbitTemplate;
  private final String exchange;

  public RabbitAccountEventPublisherAdapter(
      final RabbitTemplate rabbitTemplate,
      @Value("${messaging.exchange:domain.events}") final String exchange) {
    this.rabbitTemplate = rabbitTemplate;
    this.exchange = exchange;
  }

  @Override
  public void publishAccountCreated(final AccountModel account) {
    final DomainEventMessage message =
        new DomainEventMessage(
            EVENT_TYPE,
            LocalDateTime.now().toString(),
            Map.of(
                "id", account.getId(),
                "userId", account.getUserId(),
                "accountNumber", account.getAccountNumber(),
                "name", account.getOwnerName(),
                "email", account.getOwnerEmail(),
                "balance", account.getBalance().toPlainString(),
                "status", account.getStatus().name()));
    try {
      rabbitTemplate.convertAndSend(exchange, EVENT_TYPE, message);
      log.info(
          "[RabbitAccountEventPublisherAdapter] evento {} publicado para la cuenta {}.",
          EVENT_TYPE,
          account.getAccountNumber());
    } catch (final AmqpException exception) {
      log.error(
          "[RabbitAccountEventPublisherAdapter] no se pudo publicar {}: {}",
          EVENT_TYPE,
          exception.getMessage());
    }
  }
}
