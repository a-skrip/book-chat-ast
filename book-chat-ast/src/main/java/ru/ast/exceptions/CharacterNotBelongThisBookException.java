package ru.ast.exceptions;

public class CharacterNotBelongThisBookException extends RuntimeException {
    public CharacterNotBelongThisBookException(String message) {
        super(message);
    }
}
