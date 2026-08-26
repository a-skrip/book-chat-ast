package ru.ast.exceptions;

public class IncorrectLoginOrPasswordException extends RuntimeException {
    public IncorrectLoginOrPasswordException(String message) {
        super(message);
    }
}
