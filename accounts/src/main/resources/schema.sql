-- =============================================
-- Tabla accounts (paso 6 del diagrama)
-- Vive en la MISMA base de datos que users: crud_usuarios (TiDB).
-- Spring la ejecuta al arrancar (spring.sql.init.mode=always) y como usa
-- IF NOT EXISTS no borra ni duplica nada.
-- =============================================
CREATE TABLE IF NOT EXISTS accounts (
    id              VARCHAR(36)    NOT NULL PRIMARY KEY,
    user_id         VARCHAR(36)    NOT NULL UNIQUE,
    account_number  VARCHAR(20)    NOT NULL UNIQUE,
    owner_name      VARCHAR(100)   NOT NULL,
    owner_email     VARCHAR(150)   NOT NULL,
    balance         DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    status          VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    created_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
