package ru.ast.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.ast.dto.BookRequestDto;
import ru.ast.dto.BookResponseDto;
import ru.ast.service.BookService;

import java.util.UUID;

@RestController
@RequestMapping("/books")
@PreAuthorize("hasAnyRole('ADMIN')")
@AllArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    public ResponseEntity<BookResponseDto> addBook(@RequestBody BookRequestDto bookDto) {
        BookResponseDto response = bookService.saveBook(bookDto);
        return ResponseEntity.ofNullable(response);
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookResponseDto> getBookById(@PathVariable UUID bookId) {
        BookResponseDto response = bookService.getBook(bookId);
        return ResponseEntity.ofNullable(response);

    }

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
