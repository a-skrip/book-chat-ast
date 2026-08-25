package ru.ast.dto.response;

import lombok.Data;
import ru.ast.dto.BookDto;

import java.util.List;

@Data
public class BooksResponseDto {
    private List<BookDto> items;
}
