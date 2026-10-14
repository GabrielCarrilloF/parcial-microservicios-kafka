package com.parcial.notifiers.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.parcial.notifiers.application.port.out.EmailSenderPort;
import com.parcial.notifiers.domain.model.EmailDestinationModel;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@DisplayName("NotificationService (paso 4)")
class NotificationServiceTest {

  private final EmailSenderPort emailSenderPort = mock(EmailSenderPort.class);
  private final NotificationService service = new NotificationService(emailSenderPort);

  @Test
  @DisplayName("user.created usa la plantilla user-created.html")
  void shouldSendUserCreatedEmail() {
    final boolean sent =
        service.process(
            "user.created",
            Map.of("name", "Ada", "email", "ada@example.com", "role", "MEMBER", "status", "PENDING"));

    final EmailDestinationModel email = captureEmail();
    assertTrue(sent);
    assertEquals("ada@example.com", email.getDestinationEmail());
    assertTrue(email.getSubject().contains("creada"));
    assertTrue(email.getBody().contains("Hola, Ada"));
    assertTrue(email.getBody().contains("MEMBER"));
  }

  @Test
  @DisplayName("account.created incluye el numero de cuenta")
  void shouldSendAccountCreatedEmail() {
    service.process(
        "account.created",
        Map.of(
            "name", "Ada",
            "email", "ada@example.com",
            "accountNumber", "AC-0000000001",
            "balance", "0",
            "status", "ACTIVE"));

    assertTrue(captureEmail().getBody().contains("AC-0000000001"));
  }

  @Test
  @DisplayName("Un evento sin plantilla se ignora")
  void shouldIgnoreUnknownEvent() {
    assertFalse(service.process("user.deleted", Map.of("email", "ada@example.com")));
    verifyNoInteractions(emailSenderPort);
  }

  private EmailDestinationModel captureEmail() {
    final ArgumentCaptor<EmailDestinationModel> captor =
        ArgumentCaptor.forClass(EmailDestinationModel.class);
    verify(emailSenderPort).send(captor.capture());
    return captor.getValue();
  }
}
