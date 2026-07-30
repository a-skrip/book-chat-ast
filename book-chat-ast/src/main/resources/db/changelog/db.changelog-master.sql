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
    id          UUID PRIMARY KEY,
    book_id     UUID NOT NULL,
    name        VARCHAR(30),
    avatar_path VARCHAR(200),
    enabled     BOOLEAN DEFAULT true,
    CONSTRAINT fk_character_book
        FOREIGN KEY (book_id) REFERENCES books (id)
);

--changeset AlexeySkripnichenko:3
CREATE TABLE IF NOT EXISTS readers
(
    id UUID PRIMARY KEY,
    name VARCHAR(50)
);

--changeset AlexeySkripnichenko:4
CREATE TABLE IF NOT EXISTS reader_sessions
(
    id        UUID PRIMARY KEY,
    book_id   UUID NOT NULL,
    reader_id UUID NOT NULL,
    CONSTRAINT fk_reader_session_book
        FOREIGN KEY (book_id) REFERENCES books (id),
    CONSTRAINT fk_reader_session_reader
        FOREIGN KEY (reader_id) REFERENCES readers (id)
);

--changeset AlexeySkripnichenko:5
CREATE TABLE IF NOT EXISTS chats
(
    id         UUID PRIMARY KEY,
    session_id UUID NOT NULL,
    CONSTRAINT fk_chat_reader_session
        FOREIGN KEY (session_id) REFERENCES reader_sessions (id)
);

--changeset AlexeySkripnichenko:6
CREATE TABLE IF NOT EXISTS messages
(
    id           UUID PRIMARY KEY,
    chat_id      UUID NOT NULL,
    message_role VARCHAR(10),
    created_at TIMESTAMP DEFAULT now(),
    CONSTRAINT fk_message_chat
        FOREIGN KEY (chat_id) REFERENCES chats (id)
);





