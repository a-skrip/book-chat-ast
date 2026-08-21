package ru.ast.mapper;

import lombok.Data;
import ru.ast.dto.ReaderSessionDto;
import ru.ast.entity.ReaderSession;

@Data
public class SessionMapper {

    public static ReaderSessionDto toDto (ReaderSession entity) {
        ReaderSessionDto dto = new ReaderSessionDto();
        dto.setId(entity.getId().toString());
        dto.setBookId(entity.getBook().getId().toString());
        dto.setReaderId(entity.getReader().getId().toString());
        dto.setChats(ChatMapper.toDtoList(entity.getChats()));

        return dto;
    }
}
