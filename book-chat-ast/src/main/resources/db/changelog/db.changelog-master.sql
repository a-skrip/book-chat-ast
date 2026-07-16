--liquibase formatted sql

--changeset AlexeySkripnichenko:1
CREATE TABLE IF NOT EXISTS books
(
    id            UUID primary key,
    title         VARCHAR(50) NOT NULL,
    full_text       TEXT        NOT NULL,
    status        VARCHAR(15) default 'UPLOADED',
    uploaded_path VARCHAR(200),
    created_at    TIMESTAMP   default now(),
    updated_at    TIMESTAMP
);