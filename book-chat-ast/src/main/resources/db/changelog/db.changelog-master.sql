--liquibase formatted sql

--changeset AlexeySkripnichenko:1
CREATE TABLE IF NOT EXISTS books
(
    id            UUID PRIMARY KEY,
    title         VARCHAR(50) NOT NULL,
    full_text     TEXT        NOT NULL,
    status        VARCHAR(15) DEFAULT 'UPLOADED',
    uploaded_path VARCHAR(200),
    created_at    TIMESTAMP   default now(),
    updated_at    TIMESTAMP
);

--changeset AlexeySkripnichenko:2
CREATE TABLE IF NOT EXISTS characters
(
    id      UUID PRIMARY KEY,
    book_id UUID NOT NULL,
    name VARCHAR (30),
    avatar_path VARCHAR(200),
    enabled BOOLEAN DEFAULT true,
    CONSTRAINT fk_character_book FOREIGN KEY (book_id) REFERENCES books(id)
);

