package ru.ast.dto.response;

import lombok.Data;
import ru.ast.dto.CharacterResponseDto;
import ru.ast.dto.ChatDto;

import java.util.List;
import java.util.UUID;

@Data
public class SessionResponse {
    private String sessionId;
    private String bookId;
    private String readerId;
    private String bookTitle;
    private List<CharacterResponseDto> characters;
    private List<ChatDto> existingChats;
    private boolean isNewSession;
}
