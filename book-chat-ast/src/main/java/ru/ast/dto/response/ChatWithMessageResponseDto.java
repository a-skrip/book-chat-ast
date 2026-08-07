package ru.ast.dto.response;

import lombok.Data;
import ru.ast.dto.MessageDto;

import java.util.List;

@Data
public class ChatWithMessageResponseDto {
    private String bookId;
    private String title;
    private String characterName;
    private String readerId;
    private String chatId;
    private String readerSession;
    private List<MessageDto> messages;
}
