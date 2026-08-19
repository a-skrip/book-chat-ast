package ru.ast.dto.response;

import lombok.Data;
import ru.ast.dto.MessageDto;

import java.util.List;

@Data
public class ChatWithMessageResponseDto {
    private String bookId;
    private String title;
    private String characterName;
    private String model;
    private String reply;
    private List<MessageDto> canonChunks;
    private boolean canonSufficient;
}
