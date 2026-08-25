package ru.ast.mapper;

import ru.ast.dto.BookDto;
import ru.ast.entity.Book;

public class BookMapper {
    public static BookDto toDto(Book entity) {
        BookDto dto = new BookDto();
        dto.setId(entity.getId().toString());
        dto.setTitle(entity.getTitle());
        dto.setSlug(entity.getFullText());
        dto.setStatus(entity.getStatus().toString());
        dto.setUploadPath(entity.getUploadPath());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

}
