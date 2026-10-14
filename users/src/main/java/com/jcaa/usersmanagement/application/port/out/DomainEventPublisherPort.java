package com.jcaa.usersmanagement.application.port.out;

import com.jcaa.usersmanagement.domain.event.DomainEvent;

/**
 * Puerto de salida del paso 2 del diagrama: publicar eventos de dominio (user.created,
 * user.updated) para que el microservicio notifiers envie los correos.
 */
public interface DomainEventPublisherPort {

  void publish(DomainEvent event);
}
