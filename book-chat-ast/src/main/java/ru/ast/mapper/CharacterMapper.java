package ru.ast.mapper;

import ru.ast.dto.CharacterResponseDto;
import ru.ast.entity.Character;

import java.util.List;

public class CharacterMapper {

    public static List<CharacterResponseDto> toDtoList(List<Character> entities) {
        return entities.stream()
                .map(elem -> new CharacterResponseDto(elem.getId(),
                        elem.getName(),
                        elem.isEnabled(),
                        elem.getAvatarPath()))
                .toList();
    }

    public static CharacterResponseDto toDto(Character character) {
        return new CharacterResponseDto(
                character.getId(),
                character.getName(),
                character.isEnabled(),
                character.getAvatarPath()
        );
    }
}
