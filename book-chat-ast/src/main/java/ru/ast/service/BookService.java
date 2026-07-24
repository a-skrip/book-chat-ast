package ru.ast.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import ru.ast.dto.BookRequestDto;
import ru.ast.dto.BookResponseDto;
import ru.ast.entity.Book;
import ru.ast.exceptions.BookIsExistException;
import ru.ast.exceptions.BookNotFoundException;
import ru.ast.mapper.BookMapper;
import ru.ast.repository.BookRepository;
import ru.ast.util.TextExtractor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final FileUploadService fileUploadService;
    private final LangChainChunkingService textChunkingService;
    private final VectorStore vectorStore;

    public BookResponseDto getBook(UUID bookId) {
        return BookMapper.toDto(bookRepository.findById(bookId)
                .orElseThrow(
                        () -> new BookNotFoundException("Книга с id: " + bookId + " не найдена")));
    }

    public BookResponseDto saveBook(BookRequestDto bookRequestDto) {
        Book entity = new Book();
        String fullText;
        String uploadedPath;
        String titleFromPath = getTitleFromPath(bookRequestDto.path());

        boolean existsBookByTitle = bookRepository.existsBookByTitle(titleFromPath);

        if (!existsBookByTitle) {
            uploadedPath = fileUploadService.upload(bookRequestDto.path());
        } else {
            throw new BookIsExistException("Книга с названием: \"" + titleFromPath + "\" уже была загружена ранее");
        }

        try {
            fullText = TextExtractor.extractTextFromFile(uploadedPath);
        } catch (TikaException | IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        entity.setTitle(titleFromPath);
        entity.setUploadPath(bookRequestDto.path());
        entity.setFullText(fullText);

        Book savedBook = bookRepository.save(entity);
        log.info("Сохранена книга: \"{}\" c id: {}", titleFromPath, savedBook.getId());

        BookProcessingService processingService =
                new BookProcessingService(textChunkingService, vectorStore, bookRepository);

        log.info("Запуск ВЕКТОРИЗАЦИИ для книги с ID: {}", savedBook.getId());
        processingService.processBook(savedBook.getId());

        return BookMapper.toDto(savedBook);
    }

    private String getTitleFromPath(String path) {
        Path pathToFile = Paths.get(path);
        return pathToFile.getFileName().toString();
    }
}
