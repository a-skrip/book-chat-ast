package ru.ast.exceptions;

import java.util.UUID;

public class BookNotFoundException extends RuntimeException {

    private static final String MESSAGE = "Книга не существует в системе";

    public BookNotFoundException() {
        super(MESSAGE);
    }
    public BookNotFoundException(UUID bookId) {
        super(String.format("Книга c id: %s не существует в системе", bookId));
    }
}
