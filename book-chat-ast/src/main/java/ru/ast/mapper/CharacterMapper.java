package ru.ast.mapper;

import ru.ast.dto.CharacterDto;
import ru.ast.entity.Character;

import java.util.List;

public class CharacterMapper {

    public static List<CharacterDto> toDtoList(List<Character> entities) {
        return entities.stream()
                .map(elem -> new CharacterDto(elem.getId(), elem.getName()))
                .toList();
    }
}
