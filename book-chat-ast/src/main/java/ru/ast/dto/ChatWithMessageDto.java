package ru.ast.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatWithMessageDto {
    private String chatId;
    private String characterId;
    private List<MessageDto> messages;
}
