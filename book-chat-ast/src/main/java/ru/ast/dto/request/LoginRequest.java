package ru.ast.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequest {
    @NotNull(message = "Имя пользователя обязательно")
    private String username;
    @NotNull(message = "Пароль обязателен")
    private String password;
}
