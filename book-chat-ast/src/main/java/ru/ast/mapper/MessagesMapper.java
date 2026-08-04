package ru.ast.mapper;

import ru.ast.dto.MessageDto;
import ru.ast.entity.Message;

import java.time.LocalDate;
import java.util.List;

public class MessagesMapper {

    public static List<MessageDto> toDtoList(List<Message> messages) {
        return messages.stream()
                .map(MessagesMapper::toDto)
                .toList();
    }

    public static MessageDto toDto(Message message) {
        MessageDto dto = new MessageDto();
        dto.setRole(message.getMessageRole());
        dto.setMessage(message.getText());
        dto.setCharacter(CharacterMapper.toDto(message.getChat().getCharacter()));
        dto.setCreatedAt(LocalDate.from(message.getCreatedAt()));
        return dto;
    }
}
