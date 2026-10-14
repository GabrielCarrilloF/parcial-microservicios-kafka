package com.parcial.notifiers.application.port.in;

import java.util.Map;

/** Puerto de entrada: procesar un evento de dominio recibido de RabbitMQ y notificar por correo. */
public interface ProcessDomainEventUseCase {

  /**
   * @return true si el evento genero un correo; false si el tipo de evento no tiene plantilla.
   */
  boolean process(String eventType, Map<String, String> data);
}
