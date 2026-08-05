package ru.ast.dto;

public record RegisterAdminRequest(
        String name,
        String surname,
        String email,
        String password) {
}
