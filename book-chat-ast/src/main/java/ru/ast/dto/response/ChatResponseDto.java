package ru.ast.dto.response;

import lombok.Getter;
import lombok.Setter;
import ru.ast.dto.MessageDto;

import java.util.List;

@Getter
@Setter
public class ChatResponseDto {

    private String conversationId;
    private String sessionId;
    private String bookId;
    private String bookTitle;
    private String characterName;
    private final String model = "Mistral";
    private String reply;
    private List<MessageDto> messages;
}
