package ru.ast.dto.response;

import lombok.Data;
import ru.ast.dto.CharacterDto;

import java.util.List;

@Data
public class CharactersResponseDto {
    private String bookId;
    private List<CharacterDto> items;
}
