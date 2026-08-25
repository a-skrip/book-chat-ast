package ru.ast.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatDto {
    private String id;
    private String bookId;
    private String characterId;
    private String characterName;
    private String sessionId;
    private LocalDateTime createdAt;
    private List<MessageDto> messages;
}
