package ru.ast.dto;

import java.util.UUID;

public record ChatRequestDto(
        UUID bookId,
        UUID readerId,
        UUID characterId,
        String message) {
}
