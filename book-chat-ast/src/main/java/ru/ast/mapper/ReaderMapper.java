package ru.ast.mapper;

import ru.ast.dto.ReaderDto;
import ru.ast.entity.Reader;

import java.util.ArrayList;

public class ReaderMapper {

    public static Reader toEntity(ReaderDto readerDto) {
        Reader entity = new Reader();
        entity.setId((readerDto.getId()) == null ? null : readerDto.getId());
        entity.setName(readerDto.getName());
        entity.setSessions(new ArrayList<>());
        return entity;
    }

    public static ReaderDto toDto(Reader entity) {
        ReaderDto dto = new ReaderDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());

        return dto;
    }
}
