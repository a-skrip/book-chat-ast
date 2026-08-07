package ru.ast.dto.request;

import jakarta.validation.constraints.*;

public record RegisterAdminRequest(
        @NotBlank(message = "Имя пользователя обязательно")
        String name,
        String surname,

        @NotBlank(message = "Email обязателен")
        @Email(message = "Некорректный формат email")
        String email,

        @NotBlank(message = "Пароль обязателен")
        @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
        String password) {
}
