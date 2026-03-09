-- Crea el schema landlord antes de que Hibernate ejecute el DDL.
-- Las entidades con @Table(schema="landlord") necesitan que este schema exista.
-- Ejecutado por Spring Boot sql.init antes del DDL de Hibernate.
CREATE SCHEMA IF NOT EXISTS landlord;
