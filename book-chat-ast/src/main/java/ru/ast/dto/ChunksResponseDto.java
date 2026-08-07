package ru.ast.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChunksResponseDto {
    private  String bookId;
    private int chunkCount;
    private List<ChunkDto> allChunks;
}
