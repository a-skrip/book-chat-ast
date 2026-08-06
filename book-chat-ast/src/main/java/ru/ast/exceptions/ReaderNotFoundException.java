package ru.ast.exceptions;

import java.util.UUID;

public class ReaderNotFoundException extends RuntimeException {
    public ReaderNotFoundException(UUID uuid) {
        super(String.format("Читатель c id: %s не найден в системе", uuid));
    }
}
