package ru.ast.dto.response;

import lombok.Data;
import ru.ast.dto.MessageDto;

import java.util.List;

@Data
public class ChatWithMessageResponseDto {
    private String chatId;
    private String characterId;
    private List<MessageDto> messages;
}
