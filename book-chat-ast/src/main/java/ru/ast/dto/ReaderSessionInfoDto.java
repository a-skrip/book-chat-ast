package ru.ast.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ReaderSessionInfoDto {
    private UUID sessionId;
    private List<MessageDto> chats;
}
