package ru.ast.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.ast.dto.BookDto;
import ru.ast.dto.request.BookRequestDto;
import ru.ast.dto.response.BooksResponseDto;
import ru.ast.dto.response.ChunksResponseDto;
import ru.ast.service.BookService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("api/books")
@AllArgsConstructor
@Tag(name = "Book", description = "API для управления книгами в системе")
@SecurityRequirement(name = "bearerAuth")
public class BookController {

    private final BookService bookService;
    private final String DATA_DIR = "src/main/resources/data/";

    @Operation(summary = "Получить существующие книги")
    @GetMapping
    public ResponseEntity<BooksResponseDto> getAllBooks() {
        BooksResponseDto response = bookService.getAll();
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Добавить новую книгу",
            description = "Сохраняет книгу из указанного локального пути и разбивает на фрагменты. Тяжелая операция "
    )
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping
    public ResponseEntity<BookDto> addBook(@RequestBody BookRequestDto bookDto) {
        BookDto response = bookService.saveBook(bookDto);
        return ResponseEntity.ofNullable(response);
    }

    @Operation(
            summary = "Добавить новую книгу",
            description = """
                    Только для админов (нужен JWT).
                    Сохраняет книгу из выбранного файла. Тяжелая операция
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Книга успешно создана",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BookDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверные данные запроса",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещен (требуются права администратора)",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookDto> uploadBook(
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

    @Operation(summary = "Получените книги по ID")
    @GetMapping("/{bookId}")
    public ResponseEntity<BookDto> getBook(@PathVariable UUID bookId) {
        BookDto response = bookService.getBook(bookId);
        return ResponseEntity.ofNullable(response);
    }

    @GetMapping("/{bookId}/chunks")
    public ResponseEntity<ChunksResponseDto> getChunks(@PathVariable UUID bookId) {
        ChunksResponseDto allChunks = bookService.getAllChunks(bookId);
        return ResponseEntity.ok(allChunks);
    }

    @Operation(summary = "Удаляет книгу по ID",
            description = "Только для админов (нужен JWT)")
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
