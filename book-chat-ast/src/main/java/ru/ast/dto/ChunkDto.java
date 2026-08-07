package ru.ast.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChunkDto {
    private String index;
    private String content;
}
