package ru.ast.exceptions;

public class AdminAlreadyExistException extends RuntimeException {
    public AdminAlreadyExistException(String message) {
        super(message);
    }
}
