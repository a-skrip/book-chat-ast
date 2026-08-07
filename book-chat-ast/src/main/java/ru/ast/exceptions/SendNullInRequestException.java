package ru.ast.exceptions;

public class SendNullInRequestException extends RuntimeException{
    public SendNullInRequestException(){
        super("Передано null значение");
    }
}
