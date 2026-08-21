package ru.ast.mapper;

import ru.ast.dto.MessageDto;
import ru.ast.entity.Message;

import java.util.List;

public class MessagesMapper {

    public static MessageDto toDto(Message entity) {
        MessageDto dto = new MessageDto();
        dto.setId(entity.getId().toString());
        dto.setRole(entity.getMessageRole().toString());
        dto.setText(entity.getText());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    public static List<MessageDto> toDtoList(List<Message> messages) {
        return messages.stream()
                .map(MessagesMapper::toDto)
                .toList();
    }
}
