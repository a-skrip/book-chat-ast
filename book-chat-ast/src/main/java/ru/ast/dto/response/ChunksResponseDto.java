package ru.ast.dto.response;

import lombok.Data;
import ru.ast.dto.ChunkDto;

import java.util.List;

@Data
public class ChunksResponseDto {
    private  String bookId;
    private int chunkCount;
    private List<ChunkDto> allChunks;
}
