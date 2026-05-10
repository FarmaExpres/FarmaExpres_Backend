-- Bootstrap minimo para PostgreSQL.
-- Las estructuras y datos funcionales se gestionan con Liquibase.
-- La separacion de microservicios es logica: una base y schemas por dominio.

SELECT 'CREATE DATABASE farmaexpres'
WHERE NOT EXISTS (
    SELECT 1
    FROM pg_database
    WHERE datname = 'farmaexpres'
)\gexec

\connect farmaexpres;

CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS inventory;

REVOKE CREATE ON SCHEMA public FROM PUBLIC;
