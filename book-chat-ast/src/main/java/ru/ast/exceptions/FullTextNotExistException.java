package ru.ast.exceptions;

public class FullTextNotExistException extends RuntimeException {
    private static final String MESSAGE = "Текст для книги отсутствует";

    public FullTextNotExistException() {
        super(MESSAGE);
    }
}
