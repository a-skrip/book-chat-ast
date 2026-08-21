package ru.ast.mapper;


import ru.ast.dto.ChatDto;
import ru.ast.entity.Chat;

import java.util.List;

public class ChatMapper {

    public static ChatDto toDto(Chat entity) {
        ChatDto dto = new ChatDto();
        dto.setId(entity.getId().toString());
        dto.setBookId(entity.getSession().getBook().getId().toString());
        dto.setCharacterId(entity.getCharacter().getId().toString());
        dto.setCharacterName(entity.getCharacter().getName());
        dto.setSessionId(entity.getSession().getId().toString());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setMessages(MessagesMapper.toDtoList(entity.getMessages()));
        return dto;
    }

    public static List<ChatDto> toDtoList(List<Chat> chats) {
        return chats.stream()
                .map(ChatMapper::toDto)
                .toList();
    }
}
