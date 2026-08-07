package ru.ast.repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.ast.dto.ChunkDto;

import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
@AllArgsConstructor
public class VectorStoreRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<ChunkDto> getAllChunks(UUID bookId) {
        String query = """
                SELECT metadata ->> 'index' as index, content as chunk
                FROM vector_store
                WHERE metadata ->> 'bookId' = ?;
                """;
        try {
            List<ChunkDto> chunks = jdbcTemplate.query(
                    query,
                    new Object[]{bookId.toString()},
                    (rs, rowNum) -> new ChunkDto(
                            rs.getString("index"),
                            rs.getString("chunk")
                    )
            );
            log.info("Найдено {} чанков для книги {}", chunks.size(), bookId);
            return chunks;
        } catch (Exception e) {
            log.error("Ошибка при получении чанков для книги {}: {}", bookId, e.getMessage(), e);
            return List.of();
        }
    }
}
