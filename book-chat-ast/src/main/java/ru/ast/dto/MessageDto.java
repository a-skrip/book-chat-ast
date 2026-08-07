package ru.ast.dto;

import lombok.Data;
import ru.ast.enums.MessageRole;

import java.time.LocalDate;

@Data
public class MessageDto {
    private MessageRole role;
    private String message;
    private CharacterResponseDto character;
    private LocalDate createdAt;
}
