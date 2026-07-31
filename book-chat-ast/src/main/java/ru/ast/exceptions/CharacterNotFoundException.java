package ru.ast.exceptions;

import java.util.UUID;

public class CharacterNotFoundException extends RuntimeException {
    private static final String MESSAGE = "Персонаж не найден";

    public CharacterNotFoundException() {
        super(MESSAGE);
    }
    public CharacterNotFoundException(UUID uuid) {
        super(String.format("Персонаж c id: %s не найден", uuid));
    }
}
