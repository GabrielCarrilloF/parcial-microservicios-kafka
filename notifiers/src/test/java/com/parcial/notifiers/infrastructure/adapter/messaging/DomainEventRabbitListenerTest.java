package com.parcial.notifiers.infrastructure.adapter.messaging;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parcial.notifiers.application.port.in.ProcessDomainEventUseCase;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

@DisplayName("DomainEventRabbitListener (paso 3)")
class DomainEventRabbitListenerTest {

  private final ProcessDomainEventUseCase useCase = mock(ProcessDomainEventUseCase.class);
  private final DomainEventRabbitListener listener =
      new DomainEventRabbitListener(new ObjectMapper(), useCase);

  @Test
  @DisplayName("Convierte el JSON del evento y lo entrega al caso de uso")
  void shouldProcessEvent() throws IOException {
    listener.onMessage(
        message(
            "{\"eventName\":\"account.created\",\"occurredOn\":\"2026-10-14T10:00:00\","
                + "\"payload\":{\"email\":\"ada@example.com\"},\"extra\":\"ignorado\"}",
            "account.created"));

    verify(useCase).process("account.created", Map.of("email", "ada@example.com"));
  }

  @Test
  @DisplayName("Si el mensaje no trae eventName usa la routing key")
  void shouldUseRoutingKeyWhenEventTypeIsMissing() throws IOException {
    listener.onMessage(message("{\"payload\":{\"email\":\"a@b.com\"}}", "user.created"));

    verify(useCase).process(eq("user.created"), anyMap());
  }

  @Test
  @DisplayName("Un JSON invalido lanza excepcion para que actue el reintento")
  void shouldFailOnInvalidJson() {
    assertThrows(IOException.class, () -> listener.onMessage(message("{no-json", "user.created")));
    verify(useCase, never()).process(anyString(), anyMap());
  }

  private static Message message(final String json, final String routingKey) {
    final MessageProperties properties = new MessageProperties();
    properties.setReceivedRoutingKey(routingKey);
    return new Message(json.getBytes(StandardCharsets.UTF_8), properties);
  }
}
