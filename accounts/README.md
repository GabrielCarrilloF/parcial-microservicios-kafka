# accounts

Microservicio de cuentas del parcial (users · **accounts** · notifiers).

| Paso | Descripción | Tipo |
|------|-------------|------|
| 5 | Recibe de **users** `POST /api/accounts` | síncrono |
| 6 | Guarda la cuenta en **TiDB** (tabla `accounts` de la base `crud_usuarios`) | síncrono |
| 7 | Publica el evento `account.created` en el exchange `domain.events` (RabbitMQ) | asíncrono |

El endpoint es idempotente: si users reintenta para el mismo usuario, responde `200` con la
cuenta existente en vez de crear otra.

## Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/` | Estado del servicio |
| POST | `/api/accounts` | Crea la cuenta (`201`) o devuelve la existente (`200`) |
| GET | `/api/accounts` | Lista las cuentas |
| GET | `/api/accounts/user/{userId}` | Consulta la cuenta de un usuario |
| GET | `/swagger-ui.html` | Swagger UI |

## Variables de entorno

`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `DB_SSL_MODE`, `RABBITMQ_URL`.

## Ejecución

```bash
./mvnw clean test
./mvnw spring-boot:run   # http://localhost:8081/swagger-ui.html
```
