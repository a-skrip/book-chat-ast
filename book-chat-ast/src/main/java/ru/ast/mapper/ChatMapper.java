package ru.ast.mapper;


import ru.ast.dto.ChatDto;
import ru.ast.entity.Chat;

import java.util.ArrayList;
import java.util.List;

public class ChatMapper {

    public static ChatDto toDto(Chat chat) {
        ChatDto dto = new ChatDto();
        dto.setChatId(chat.getId().toString());
        dto.setSessionId(chat.getSession().getId().toString());
        dto.setCharacterId(chat.getCharacter().getId().toString());

        return dto;
    }

    public static List<ChatDto> toDtoList(List<Chat> chats) {
        List<ChatDto> dtos = new ArrayList<>();
        return chats.stream()
                .map(ChatMapper::toDto)
                .toList();
    }
}
