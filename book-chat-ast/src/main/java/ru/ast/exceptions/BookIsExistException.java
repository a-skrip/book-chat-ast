package ru.ast.exceptions;

public class BookIsExistException extends RuntimeException {
    public BookIsExistException(String message) {
        super(message);
    }
}
