package ru.ast.exceptions;

public class NameNoSendException extends RuntimeException {
    private static final String MESSAGE = "Не передано имя";

    public NameNoSendException() {
        super(MESSAGE);
    }
}
