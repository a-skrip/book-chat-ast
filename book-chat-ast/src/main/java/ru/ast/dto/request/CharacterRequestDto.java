package ru.ast.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CharacterRequestDto {
    private String shortDescription;
    private String promptStyle;
    private Boolean enabled;
    private String avatarPath;
}
