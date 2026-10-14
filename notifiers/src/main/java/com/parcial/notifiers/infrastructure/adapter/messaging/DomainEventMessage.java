package com.parcial.notifiers.infrastructure.adapter.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

/**
 * Mensaje JSON que viaja por el exchange {@code domain.events} de RabbitMQ.
 *
 * <p>Mismo formato del evento de dominio del docente (notification-service): nombre del evento,
 * fecha y payload.
 *
 * <pre>
 * {
 *   "eventName": "user.created | user.updated | account.created",
 *   "occurredOn": "2026-10-14T10:00:00",
 *   "payload": { "name": "...", "email": "...", ... }
 * }
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DomainEventMessage(String eventName, String occurredOn, Map<String, String> payload) {}
