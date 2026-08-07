package ru.ast.mapper;

import ru.ast.dto.response.BookResponseDto;
import ru.ast.entity.Book;

public class BookMapper {
    public static BookResponseDto toDto(Book entity) {
        BookResponseDto dto = new BookResponseDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setFullText((entity.getFullText()));
        dto.setStatus(entity.getStatus().toString());
        dto.setUploadPath(entity.getUploadPath());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

}
