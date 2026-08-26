package ru.ast.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AdminResponse {
    private UUID id;
    private String username;
    private String email;
    private String role;
    private LocalDateTime createdAt;
}
