package ru.ast.mapper;

import ru.ast.dto.CharacterDto;
import ru.ast.entity.Character;

import java.util.List;

public class CharacterMapper {

    public static List<CharacterDto> toDtoList(List<Character> entities) {
        return entities.stream()
                .map(CharacterMapper::toDto)
                .toList();
    }

    public static CharacterDto toDto(Character entity) {
        return new CharacterDto(
                entity.getId().toString(),
                entity.getBook().getId().toString(),
                entity.getName(),
                entity.getShortDescription() == null ? null : entity.getShortDescription(),
                entity.getPromptStyle() == null ? null : entity.getPromptStyle(),
                entity.isEnabled()
        );
    }
}
