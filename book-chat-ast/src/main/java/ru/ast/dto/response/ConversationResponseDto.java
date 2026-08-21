package ru.ast.dto.response;

import lombok.Data;
import ru.ast.dto.ChatDto;

import java.util.List;

@Data
public class ConversationResponseDto {
       private List<ChatDto> items;
}
