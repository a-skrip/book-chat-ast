package ru.ast.dto;

import java.util.UUID;

public record CharacterResponseDto(UUID characterId,
                                   String name,
                                   Boolean isEnabled,
                                   String avatarPath) {
}
