package ru.ast.dto;

import lombok.Data;
import ru.ast.enums.MessageRole;

import java.time.LocalDateTime;

@Data
public class MessageDto {
    private MessageRole role;
    private String message;
    private LocalDateTime createdAt;
}
