package ru.ast.dto.response;

import lombok.Data;
import ru.ast.dto.MessageDto;

import java.util.List;
import java.util.UUID;

@Data
public class ReaderSessionResponseDto {
    private UUID sessionId;
    private List<MessageDto> chats;
}
