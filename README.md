# Parcial de microservicios: comunicación síncrona y asíncrona con resiliencia

Fundación Universitaria Colombo Internacional (UNICOLOMBO) · Electiva · Décimo semestre
Docente: John Carlos Arrieta Arrieta

**Integrantes**

| Integrante | Microservicio |
|------------|---------------|
| Gabriel Enrique Carrillo Florez | `users` |
| Jesús Manuel Valiente Florez | `accounts` |
| Jefferson Arrieta Durán | `notifiers` |

![Diagrama del parcial](docs/diagrama-parcial.jpg)

## Flujo

| Paso | Origen → destino | Tipo |
|------|------------------|------|
| 1 | users → TiDB (tabla `users`) | Síncrono |
| 2 | users → RabbitMQ (`user.created`, `user.updated`) | Asíncrono |
| 3 | RabbitMQ (`notification.events.queue`) → notifiers | Asíncrono |
| 4 | notifiers → Brevo (API REST) | Síncrono |
| 5 | users → accounts (`POST /api/accounts`) | Síncrono |
| 6 | accounts → TiDB (tabla `accounts`) | Síncrono |
| 7 | accounts → RabbitMQ (`account.created`) | Asíncrono |

## Estructura del repositorio

```
users/       Microservicio de usuarios (repositorio base del docente, arquitectura hexagonal)
accounts/    Microservicio de cuentas
notifiers/   Microservicio de notificaciones (envío de correos migrado desde users)
docs/        Diagrama del parcial
```

## Mensajería: RabbitMQ en CloudAMQP (patrón FanIn)

| Elemento | Valor |
|----------|-------|
| Exchange | `domain.events` (tipo `topic`, durable) |
| Cola | `notification.events.queue` (durable) |
| Bindings | `user.created`, `user.updated`, `account.created` |

Formato del evento (igual al del *notification-service* del docente):

```json
{ "eventName": "user.created", "occurredOn": "2026-10-14T10:00:00", "payload": { "name": "...", "email": "..." } }
```

notifiers declara el exchange, la cola y los bindings al iniciar, por lo que coinciden con
los creados en CloudAMQP.

## Resiliencia

* users → accounts: timeout, retry, circuit breaker y fallback (Resilience4j).
* notifiers → Brevo: retry con backoff exponencial y circuit breaker (Resilience4j).
* Publicación en RabbitMQ: reintentos del `RabbitTemplate` y fallback que registra el error.
* Consumo en notifiers: reintentos del contenedor de Spring AMQP.

## Despliegue en Render

Un *Web Service* por microservicio desde este repositorio, con *Runtime* **Docker** y
*Root Directory* `users`, `accounts` o `notifiers`. notifiers debe permanecer activo para
consumir la cola (en el plan gratuito, una tarea programada que consulte `/` cada 13 minutos).

| Servicio | Variables de entorno |
|----------|----------------------|
| users | `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `DB_SSL_MODE`, `JWT_SECRET`, `RABBITMQ_URL`, `ACCOUNTS_BASE_URL` |
| accounts | `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `DB_SSL_MODE`, `RABBITMQ_URL` |
| notifiers | `RABBITMQ_URL`, `BREVO_API_KEY`, `BREVO_FROM_ADDRESS`, `BREVO_FROM_NAME` |
