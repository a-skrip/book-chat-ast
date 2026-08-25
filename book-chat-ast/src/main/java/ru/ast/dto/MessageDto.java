package ru.ast.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDto {
    private String id;
    private String role;
    private String text;
    private LocalDateTime createdAt;
}
