package com.parcial.notifiers.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologia FanIn en RabbitMQ (igual a la configurada en CloudAMQP segun la Parte 6): exchange
 * topic domain.events, cola durable notification.events.queue y un binding por evento. Si ya
 * existen en CloudAMQP, la declaracion no los modifica.
 */
@Configuration(proxyBeanMethods = false)
public class RabbitTopologyConfig {

  @Bean
  public Declarables notificationTopology(
      @Value("${messaging.exchange:domain.events}") final String exchangeName,
      @Value("${messaging.queue:notification.events.queue}") final String queueName) {
    final TopicExchange exchange = new TopicExchange(exchangeName, true, false);
    final Queue queue = QueueBuilder.durable(queueName).build();
    final Binding userCreated = BindingBuilder.bind(queue).to(exchange).with("user.created");
    final Binding userUpdated = BindingBuilder.bind(queue).to(exchange).with("user.updated");
    final Binding accountCreated = BindingBuilder.bind(queue).to(exchange).with("account.created");
    return new Declarables(exchange, queue, userCreated, userUpdated, accountCreated);
  }
}
