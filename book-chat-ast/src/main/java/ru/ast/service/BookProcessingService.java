package ru.ast.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ast.entity.Book;
import ru.ast.exceptions.FullTextNotExistException;
import ru.ast.repository.BookRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookProcessingService {

    private final TextChunkingService chunkingService;
    private final VectorStore vectorStore;
    private final BookRepository bookRepository;

    private static final int BATCH_SIZE = 3;

    @Transactional
    public void processBook(UUID bookId) {
        long startTime = System.currentTimeMillis();
        log.info("Начинаем обработку книги с ID: {}", bookId);

        // 1. Получаем текст
        String fullText = getFullTextFromDatabase(bookId);
        if (fullText == null || fullText.isEmpty()) {
            log.error("Текст для книги {} не найден", bookId);
            return;
        }

        // 2. Разбиваем на чанки
        List<Document> allChunks = chunkingService.splitText(fullText, bookId);
        log.info("Создано {} чанков", allChunks.size());

        if (allChunks.isEmpty()) {
            log.warn("Не удалось создать чанки для книги {}", bookId);
            return;
        }

        // 3. Сохраняем батчами с прогрессом
        log.info("Начинаем сохранение в VectorStore батчами по {} чанков", BATCH_SIZE);

        int totalChunks = allChunks.size();
        int processed = 0;

        for (int i = 0; i < totalChunks; i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, totalChunks);
            List<Document> batch = allChunks.subList(i, end);

            long batchStart = System.currentTimeMillis();
            vectorStore.add(batch);
            long batchTime = System.currentTimeMillis() - batchStart;

            processed += batch.size();
            log.info("✅ Прогресс: {}/{} чанков (батч {} чанков за {} мс)",
                    processed, totalChunks, batch.size(), batchTime);
        }

        long totalTime = System.currentTimeMillis() - startTime;
        log.info("✅ Книга {} обработана за {} мс ({} чанков)",
                bookId, totalTime, totalChunks);
    }



    private String getFullTextFromDatabase(UUID bookId) {
        return bookRepository.findById(bookId)
                .map(Book::getFullText)
                .orElseThrow(() -> new FullTextNotExistException("Текст книги не найден или отсутствует"));
    }

    private void deleteOldChunks(UUID bookId) {
        // Ваша логика удаления старых чанков
        // Можно удалить из vector_store по метаданным
        log.info("Удаляем старые чанки для книги {}", bookId);
    }

    private void updateBookStatus(UUID bookId, int totalChunks) {
        // Обновляем статус в таблице works
        log.info("Обновляем статус книги {}: {} чанков", bookId, totalChunks);
    }
}
