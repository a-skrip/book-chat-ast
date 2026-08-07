package ru.ast.dto;

import lombok.Data;

import java.util.List;

@Data
public class CharactersResponseDto {
    private String bookId;
    private List<CharacterResponseDto> characters;
}
