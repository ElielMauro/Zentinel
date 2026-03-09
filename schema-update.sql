-- Zentinel - Script de actualización de Base de Datos
-- Ejecutar en PostgreSQL para añadir la columna password requerida por Spring Security

ALTER TABLE usuarios 
ADD COLUMN IF NOT EXISTS password VARCHAR(255) NOT NULL DEFAULT '';
