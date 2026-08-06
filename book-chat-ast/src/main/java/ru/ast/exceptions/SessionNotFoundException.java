package ru.ast.exceptions;

import java.util.UUID;

public class SessionNotFoundException extends RuntimeException {

    public SessionNotFoundException(UUID uuid) {
        super(String.format("Пользовательская сессия с id: %s не существует", uuid));
    }
}
