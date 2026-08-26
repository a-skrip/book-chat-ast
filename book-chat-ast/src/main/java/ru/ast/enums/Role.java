package ru.ast.enums;

public enum Role {
    USER,
    ADMIN;

    // Для Spring Security
    public String getAuthority() {
        return name();
    }
}
