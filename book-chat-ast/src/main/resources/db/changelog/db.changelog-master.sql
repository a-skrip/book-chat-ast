--liquibase formatted sql

--changeset AlexeySkripnichenko:1
CREATE TABLE IF NOT EXISTS books
(
    id            UUID PRIMARY KEY ,
    title         VARCHAR(50) NOT NULL,
    full_text     TEXT        NOT NULL,
    status        VARCHAR(15) DEFAULT 'UPLOADED',
    uploaded_path VARCHAR(200),
    created_at    TIMESTAMP   default now(),
    updated_at    TIMESTAMP
);