package ru.ast.dto;

import java.util.UUID;

public record ModelResponseDto(String question, String answer, UUID bookId) {
}
