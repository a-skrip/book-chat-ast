package ru.ast.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ast.dto.BookRequestDto;
import ru.ast.dto.BookResponseDto;
import ru.ast.service.BookService;

import java.util.UUID;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<BookResponseDto> addBook(@RequestBody BookRequestDto bookDto) {
        bookService.saveBook(bookDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDto> getBookById(@RequestParam UUID bookId) {
        BookResponseDto responseDto = bookService.getBook(bookId);
        return ResponseEntity.ofNullable(responseDto);
    }
}
