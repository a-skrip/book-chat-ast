package ru.ast.exceptions;

import java.util.UUID;

public class BookIsExistException extends RuntimeException {
    private final static String MESSAGE = "Книга уже существует";

    public BookIsExistException() {
        super(MESSAGE);
    }
    public BookIsExistException(String title) {
        super(String.format("Книга: \"%s\" уже существует", title));
    }
}
