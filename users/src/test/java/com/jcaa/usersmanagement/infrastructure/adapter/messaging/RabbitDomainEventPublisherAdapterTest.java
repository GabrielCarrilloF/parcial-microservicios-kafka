package com.jcaa.usersmanagement.infrastructure.adapter.messaging;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.enums.UserStatus;
import com.jcaa.usersmanagement.domain.event.UserCreatedDomainEvent;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserEmail;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.domain.valueobject.UserName;
import com.jcaa.usersmanagement.domain.valueobject.UserPassword;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@DisplayName("RabbitDomainEventPublisherAdapter (paso 2)")
class RabbitDomainEventPublisherAdapterTest {

  private final RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
  private final RabbitDomainEventPublisherAdapter adapter =
      new RabbitDomainEventPublisherAdapter(rabbitTemplate, "domain.events");

  @Test
  @DisplayName("Publica user.created en el exchange domain.events con routing key user.created")
  void shouldPublishUserCreated() {
    adapter.publish(new UserCreatedDomainEvent(user()));

    final ArgumentCaptor<Object> message = ArgumentCaptor.forClass(Object.class);
    verify(rabbitTemplate).convertAndSend(eq("domain.events"), eq("user.created"), message.capture());
    final DomainEventMessage event = (DomainEventMessage) message.getValue();
    assertEquals("user.created", event.eventName());
    assertNotNull(event.occurredOn());
    assertEquals("ada@example.com", event.payload().get("email"));
    assertEquals("Ada Lovelace", event.payload().get("name"));
  }

  @Test
  @DisplayName("Si RabbitMQ no esta disponible no propaga la excepcion (fallback)")
  void shouldNotFailWhenBrokerIsDown() {
    doThrow(new AmqpConnectException(new RuntimeException("sin conexion")))
        .when(rabbitTemplate)
        .convertAndSend(anyString(), anyString(), any(Object.class));

    assertDoesNotThrow(() -> adapter.publish(new UserCreatedDomainEvent(user())));
  }

  private static UserModel user() {
    return new UserModel(
        new UserId("u-01"),
        new UserName("Ada Lovelace"),
        new UserEmail("ada@example.com"),
        UserPassword.fromPlainText("Pass1234"),
        UserRole.MEMBER,
        UserStatus.PENDING);
  }
}
