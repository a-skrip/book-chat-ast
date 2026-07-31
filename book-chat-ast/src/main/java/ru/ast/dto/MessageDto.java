package ru.ast.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDto {
    private String role;
    private String message;
    private LocalDateTime timestamp;
}
