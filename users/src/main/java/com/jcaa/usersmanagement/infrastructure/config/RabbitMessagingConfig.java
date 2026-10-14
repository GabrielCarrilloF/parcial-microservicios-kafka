package com.jcaa.usersmanagement.infrastructure.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Los eventos se envian a RabbitMQ como JSON. Spring Boot asigna este conversor al RabbitTemplate
 * autoconfigurado (la conexion se toma de spring.rabbitmq.addresses = RABBITMQ_URL).
 */
@Configuration(proxyBeanMethods = false)
public class RabbitMessagingConfig {

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}
