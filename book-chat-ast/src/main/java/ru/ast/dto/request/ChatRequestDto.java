package ru.ast.dto.request;

import java.util.UUID;

public record ChatRequestDto(
        UUID bookId,
        UUID readerId,
        UUID characterId,
        UUID sessionId,
        String message) {}
