package ru.ast.exceptions;

public class FullTextNotExistException extends RuntimeException {
    public FullTextNotExistException(String message) {
        super(message);
    }
}
