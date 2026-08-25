package ru.ast.dto;

public record CharacterDto(
        String id,
        String bookId,
        String name,
        String shortDescription,
        String promptStyle,
        boolean enabled
        ) {
}
