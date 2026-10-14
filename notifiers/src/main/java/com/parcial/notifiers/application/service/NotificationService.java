package com.parcial.notifiers.application.service;

import com.parcial.notifiers.application.port.in.ProcessDomainEventUseCase;
import com.parcial.notifiers.application.port.out.EmailSenderPort;
import com.parcial.notifiers.domain.exception.EmailSenderException;
import com.parcial.notifiers.domain.model.EmailDestinationModel;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Paso 4 del diagrama: arma el correo segun el tipo de evento y lo envia por el puerto
 * EmailSenderPort (adaptador de Brevo).
 *
 * <p>Logica migrada desde EmailNotificationService del microservicio users: carga la plantilla
 * HTML y reemplaza los marcadores {{campo}} con los datos del evento.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService implements ProcessDomainEventUseCase {

  private static final Map<String, EmailTemplate> TEMPLATES =
      Map.of(
          "user.created",
          new EmailTemplate("user-created.html", "Tu cuenta ha sido creada — Gestion de Usuarios"),
          "user.updated",
          new EmailTemplate("user-updated.html", "Tu cuenta ha sido actualizada — Gestion de Usuarios"),
          "account.created",
          new EmailTemplate("account-created.html", "Tu cuenta bancaria ha sido creada"));

  private final EmailSenderPort emailSenderPort;

  @Override
  public boolean process(final String eventType, final Map<String, String> data) {
    final EmailTemplate template = TEMPLATES.get(eventType);
    if (Objects.isNull(template)) {
      log.warn("[NotificationService] evento '{}' sin plantilla de correo; se ignora.", eventType);
      return false;
    }
    final String body = render(loadTemplate(template.fileName()), data);
    emailSenderPort.send(
        new EmailDestinationModel(data.get("email"), data.get("name"), template.subject(), body));
    log.info("[NotificationService] correo de '{}' enviado a {}.", eventType, data.get("email"));
    return true;
  }

  private String loadTemplate(final String fileName) {
    final String path = "/templates/" + fileName;
    try (InputStream inputStream = getClass().getResourceAsStream(path)) {
      if (Objects.isNull(inputStream)) {
        throw EmailSenderException.becauseSendFailed(
            new IllegalStateException("Template not found: " + path));
      }
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (final IOException ioException) {
      throw EmailSenderException.becauseSendFailed(ioException);
    }
  }

  private static String render(final String template, final Map<String, String> values) {
    String result = template;
    for (final Map.Entry<String, String> entry : values.entrySet()) {
      result = result.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
    }
    return result;
  }

  private record EmailTemplate(String fileName, String subject) {}
}
