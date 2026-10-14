# notifiers

Microservicio de notificaciones del parcial (users · accounts · **notifiers**).

| Paso | Descripción | Tipo |
|------|-------------|------|
| 3 | Consume la cola `notification.events.queue` de RabbitMQ (CloudAMQP) | asíncrono |
| 4 | Envía el correo con la API REST de Brevo | síncrono |

Patrón **FanIn**: users (`user.created`, `user.updated`) y accounts (`account.created`) publican
en el exchange `domain.events`; notifiers es el único consumidor de la cola.

El código de envío de correos (adaptador de Brevo con retry y circuit breaker, plantillas HTML y
modelo `EmailDestinationModel`) se migró desde el microservicio users.

## Variables de entorno

`RABBITMQ_URL`, `BREVO_API_KEY`, `BREVO_FROM_ADDRESS`, `BREVO_FROM_NAME`.

## Ejecución

```bash
./mvnw clean test
./mvnw spring-boot:run   # http://localhost:8082/
```
