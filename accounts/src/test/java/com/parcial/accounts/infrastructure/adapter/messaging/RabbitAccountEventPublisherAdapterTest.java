package com.parcial.accounts.infrastructure.adapter.messaging;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.parcial.accounts.domain.model.AccountModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@DisplayName("RabbitAccountEventPublisherAdapter (paso 7)")
class RabbitAccountEventPublisherAdapterTest {

  private final RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
  private final RabbitAccountEventPublisherAdapter adapter =
      new RabbitAccountEventPublisherAdapter(rabbitTemplate, "domain.events");
  private final AccountModel account =
      AccountModel.openFor("u-01", "Ada Lovelace", "ada@example.com", "AC-0000000001");

  @Test
  @DisplayName("Publica account.created en domain.events con los datos de la cuenta")
  void shouldPublishAccountCreated() {
    adapter.publishAccountCreated(account);

    final ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
    verify(rabbitTemplate)
        .convertAndSend(eq("domain.events"), eq("account.created"), captor.capture());
    final DomainEventMessage message = (DomainEventMessage) captor.getValue();
    assertEquals("account.created", message.eventName());
    assertEquals("AC-0000000001", message.payload().get("accountNumber"));
    assertEquals("ada@example.com", message.payload().get("email"));
    assertEquals("Ada Lovelace", message.payload().get("name"));
  }

  @Test
  @DisplayName("Si RabbitMQ no esta disponible no propaga la excepcion (fallback)")
  void shouldNotFailWhenBrokerIsDown() {
    doThrow(new AmqpConnectException(new RuntimeException("sin conexion")))
        .when(rabbitTemplate)
        .convertAndSend(anyString(), anyString(), any(Object.class));

    assertDoesNotThrow(() -> adapter.publishAccountCreated(account));
  }
}
