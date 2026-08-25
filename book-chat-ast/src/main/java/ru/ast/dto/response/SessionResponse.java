package ru.ast.dto.response;

import lombok.Data;
import ru.ast.dto.CharacterDto;
import ru.ast.dto.ChatDto;

import java.util.List;

@Data
public class SessionResponse {
    private String sessionId;
    private String bookId;
    private String readerId;
    private String bookTitle;
    private List<CharacterDto> characters;
    private List<ChatDto> existingChats;
    private boolean isNewSession;
}
