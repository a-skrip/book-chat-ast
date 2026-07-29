package ru.ast.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ast.dto.CharacterDto;
import ru.ast.entity.Book;
import ru.ast.exceptions.BookNotFoundException;
import ru.ast.repository.BookRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor

public class BookProcessingService {

    private final LangChainChunkingService chunkingService;
    private final VectorStore vectorStore;
    private final BookRepository bookRepository;
    private final CharacterExtractionService characterExtractor;

    private static final int BATCH_SIZE = 20;
    private static final int SLEEP_TIME = 30_000;

    @Transactional
    public void processBook(UUID bookId) {
        long startTime = System.currentTimeMillis();
        log.info("Начинаем обработку книги с ID: {}", bookId);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(
                        () -> new BookNotFoundException("Книга не найдена")
                );

        String fullText = book.getFullText();
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
            long startBatch = System.currentTimeMillis();
            int end = Math.min(i + BATCH_SIZE, totalChunks);
            List<Document> batch = allChunks.subList(i, end);

            vectorStore.add(batch);
            long batchTime = System.currentTimeMillis() - startBatch;

            processed += batch.size();
            log.info("✅ Прогресс: {}/{} чанков (батч {} чанков за {} мс)",
                    processed, totalChunks, batch.size(), batchTime);

            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        log.info("✅ Книга {} обработана за {} мс ({} чанков)",
                bookId, totalTime, totalChunks);

        book.setStatus("PROCESSED");
        log.info("Сохранено, статус изменен на PROCESSED");
        bookRepository.save(book);

        String status = bookRepository.findById(bookId).orElseThrow().getStatus();
        log.info("Статус для bookId {} - {}", bookId, status);

        List<CharacterDto> characters = characterExtractor.findCharacters(bookId);
        log.info("В произведении найдено: {} персонажей. Characters: {}"
                , characters.size(), characters.toString());
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
