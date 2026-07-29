package ru.ast.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
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
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final FileUploadService fileUploadService;
    private final BookProcessingService bookProcessingService;

    public BookResponseDto getBook(UUID bookId) {
        return BookMapper.toDto(bookRepository.findById(bookId)
                .orElseThrow(
                        () -> new BookNotFoundException("Книга с id: " + bookId + " не найдена")));
    }

    public BookResponseDto saveBook(BookRequestDto bookRequestDto) {
        Book entity = new Book();
        String fullText;
        String uploadedPath = bookRequestDto.path();
        String title = bookRequestDto.title();
        String pathToLocalFile;
        boolean existsBookByTitle = bookRepository.existsBookByTitle(title);

        if (!existsBookByTitle) {
            pathToLocalFile = fileUploadService.upload(uploadedPath);
        } else {
            throw new BookIsExistException("Книга с названием: \"" + title + "\" уже была загружена ранее");
        }

        try {
            fullText = TextExtractor.extractTextFromFile(pathToLocalFile);
        } catch (TikaException | IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        entity.setTitle(title);
        entity.setUploadPath(uploadedPath);
        entity.setFullText(fullText);

        Book savedBook = bookRepository.save(entity);
        UUID bookId = savedBook.getId();
        log.info("Сохранена книга: \"{}\" c id: {}", title, bookId);

        log.info("Запуск ВЕКТОРИЗАЦИИ для книги с ID: {}", bookId);
        bookProcessingService.processBook(bookId);

        return BookMapper.toDto(savedBook);
    }
}
