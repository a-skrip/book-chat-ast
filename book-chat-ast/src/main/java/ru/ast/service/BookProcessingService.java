package ru.ast.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import ru.ast.dto.CharactersResponseDto;
import ru.ast.entity.Book;
import ru.ast.enums.BookStatus;
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
    private final CharacterService characterService;

    private static final int BATCH_SIZE = 10;
    private static final int SLEEP_TIME = 30_000;


    public void processBook(UUID bookId) {
        long startTime = System.currentTimeMillis();
        log.info("Начинаем обработку книги с ID: {}", bookId);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        String fullText = book.getFullText();

        if (fullText == null || fullText.isEmpty()) {
            log.error("Текст для книги {} не найден", bookId);
            return;
        }

        if (book.getStatus().equals(BookStatus.UPLOADED)) {
            // 2. Разбиваем на чанки
            List<Document> allChunks = chunkingService.splitText(fullText, book.getId());

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

                try {
                    vectorStore.add(batch);
                    log.info("Батч: {} сохранен", i);

                } catch (Exception e) {
                    log.warn("Не удалось сохранить батч в БД  " + e.getMessage());
                }

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
                    book.getId(), totalTime, totalChunks);

            book.setStatus(BookStatus.PROCESSED);
            log.info("Сохранено, статус изменен на PROCESSED");
            Book savedBook = bookRepository.save(book);

            log.info("Статус для bookId {} - {}", bookId, savedBook.getStatus());

            CharactersResponseDto characters = characterService.extractCharacters(savedBook.getId());
            log.info("В книге id: {}, найдено: {} персонажей",
                    savedBook.getId(),
                    characters.getCharacters().size());
        }
    }

}
