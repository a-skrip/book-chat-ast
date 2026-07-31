package ru.ast.exceptions;

import java.util.UUID;

public class ChatNotFoundException extends RuntimeException {
    public ChatNotFoundException(UUID uuid) {
        super(String.format("Чат с id: %s не найден", uuid));
    }

}
