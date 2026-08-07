package ru.ast.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.ast.dto.request.BookRequestDto;
import ru.ast.dto.response.BookResponseDto;
import ru.ast.dto.response.ChunksResponseDto;
import ru.ast.service.BookService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/books")
@AllArgsConstructor
public class BookController {

    private final BookService bookService;
    private final String DATA_DIR = "src/main/resources/data/";

    @PostMapping
    public ResponseEntity<BookResponseDto> addBook(@RequestBody BookRequestDto bookDto) {
        BookResponseDto response = bookService.saveBook(bookDto);
        return ResponseEntity.ofNullable(response);
    }

    // Новый эндпоинт для загрузки файла через форму
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookResponseDto> uploadBook(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title) {

        try {
            // Сохраняем файл на сервере
            Path dataPath = Paths.get(DATA_DIR);
            if (!Files.exists(dataPath)) {
                Files.createDirectories(dataPath);
            }

            String filename = file.getOriginalFilename();
            Path filePath = dataPath.resolve(filename);
            Files.write(filePath, file.getBytes());

            // Создаем DTO с путем к сохраненному файлу
            BookRequestDto bookDto = new BookRequestDto(filePath.toString(), title);

            // Вызываем существующую логику
            return ResponseEntity.ok(bookService.saveBook(bookDto));

        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookResponseDto> getBook(@PathVariable UUID bookId) {
        BookResponseDto response = bookService.getBook(bookId);
        return ResponseEntity.ofNullable(response);
    }

    @GetMapping
    public ResponseEntity<List<BookResponseDto>> getAllBooks() {
        List<BookResponseDto> response = bookService.getAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{bookId}/chunks")
    public ResponseEntity<ChunksResponseDto> getChunks(@PathVariable UUID bookId) {
        ChunksResponseDto allChunks = bookService.getAllChunks(bookId);
        return ResponseEntity.ok(allChunks);
    }


    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/{bookId}")
    public ResponseEntity<String> deleteBook(@PathVariable UUID bookId) {
        boolean deleted = bookService.deleteBook(bookId);
        if (deleted) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
