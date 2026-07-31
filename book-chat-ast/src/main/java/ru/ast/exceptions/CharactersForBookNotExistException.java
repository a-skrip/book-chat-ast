package ru.ast.exceptions;

import java.util.UUID;

public class CharactersForBookNotExistException extends RuntimeException {
    public CharactersForBookNotExistException(UUID uuid) {
        super(String.format("Героев для книги c id: %s не существует", uuid));
    }
}
