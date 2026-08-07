package ru.ast.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CharacterRequestDto {
    private Boolean enabled;
    private String avatarPath;
}
