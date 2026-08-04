package ru.ast.dto;

import lombok.Data;
import ru.ast.enums.MessageRole;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MessageDto {
    private MessageRole role;
    private String message;
    private CharacterDto character;
    private LocalDate createdAt;
}
