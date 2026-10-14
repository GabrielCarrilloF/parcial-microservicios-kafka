package com.parcial.notifiers.infrastructure.adapter.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parcial.notifiers.application.port.in.ProcessDomainEventUseCase;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Paso 3 del diagrama (asincrono): consumidor unico de la cola notification.events.queue
 * (patron FanIn: users y accounts publican, notifiers consume).
 *
 * <p>Si el procesamiento falla, Spring AMQP reintenta segun spring.rabbitmq.listener.simple.retry.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventRabbitListener {

  private final ObjectMapper objectMapper;
  private final ProcessDomainEventUseCase processDomainEventUseCase;

  @RabbitListener(queues = "${messaging.queue:notification.events.queue}")
  public void onMessage(final Message message) throws IOException {
    final DomainEventMessage event =
        objectMapper.readValue(message.getBody(), DomainEventMessage.class);
    final String routingKey = message.getMessageProperties().getReceivedRoutingKey();
    final String eventName = Objects.isNull(event.eventName()) ? routingKey : event.eventName();
    log.info(
        "[DomainEventRabbitListener] evento recibido: {} ({})", eventName, event.occurredOn());
    processDomainEventUseCase.process(
        eventName, Objects.isNull(event.payload()) ? Map.of() : event.payload());
  }
}
