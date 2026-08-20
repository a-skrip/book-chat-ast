package ru.ast.dto.response;

import lombok.Data;
import ru.ast.enums.MessageRole;

@Data
public class ConversationDto {

    private MessageRole role;
    private String message;
}
