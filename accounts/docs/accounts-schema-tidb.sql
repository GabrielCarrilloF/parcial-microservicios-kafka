-- =============================================
-- Script para ejecutar A MANO en el SQL Editor de TiDB Cloud (opcional).
-- accounts crea esta tabla solo al arrancar; este script sirve para
-- verificarla o crearla antes del primer despliegue.
-- =============================================
USE crud_usuarios;

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

-- Evidencia para la sustentacion: cada usuario con su cuenta
SELECT u.id, u.name, u.email, a.account_number, a.balance, a.status, a.created_at
FROM users u
LEFT JOIN accounts a ON a.user_id = u.id
ORDER BY a.created_at DESC;
