package com.jcaa.usersmanagement.infrastructure.adapter.messaging;

import com.jcaa.usersmanagement.application.port.out.DomainEventPublisherPort;
import com.jcaa.usersmanagement.domain.event.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Paso 2 del diagrama (asincrono): publica los eventos de dominio de users en el exchange
 * {@code domain.events} (tipo topic) de RabbitMQ en CloudAMQP.
 *
 * <p>La routing key es el nombre del evento ({@code user.created}, {@code user.updated}); los
 * bindings del exchange los dirigen a la cola {@code notification.events.queue} que consume
 * notifiers (patron FanIn).
 *
 * <p>Fallback: si RabbitMQ no esta disponible (tras los reintentos del RabbitTemplate), el error se
 * registra y la operacion del usuario no se revierte.
 */
@Slf4j
@Component
public class RabbitDomainEventPublisherAdapter implements DomainEventPublisherPort {

  private final RabbitTemplate rabbitTemplate;
  private final String exchange;

  public RabbitDomainEventPublisherAdapter(
      final RabbitTemplate rabbitTemplate,
      @Value("${messaging.exchange:domain.events}") final String exchange) {
    this.rabbitTemplate = rabbitTemplate;
    this.exchange = exchange;
  }

  @Override
  public void publish(final DomainEvent event) {
    final DomainEventMessage message =
        new DomainEventMessage(
            event.getEventName(), event.getOccurredOn().toString(), event.payload());
    try {
      rabbitTemplate.convertAndSend(exchange, event.getEventName(), message);
      log.info(
          "[RabbitDomainEventPublisherAdapter] evento {} publicado en '{}'.",
          event.getEventName(),
          exchange);
    } catch (final AmqpException exception) {
      log.error(
          "[RabbitDomainEventPublisherAdapter] no se pudo publicar {}: {}",
          event.getEventName(),
          exception.getMessage());
    }
  }
}
