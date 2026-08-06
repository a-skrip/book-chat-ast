package ru.ast.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.ast.dto.ReaderDto;
import ru.ast.service.ReaderService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReaderController {

    private final ReaderService readerService;

    @PostMapping("/readers")
    public ResponseEntity<ReaderDto> createReader(@RequestParam String name) {
        ReaderDto readerDto = readerService.saveReader(name);
        return ResponseEntity.ok(readerDto);

    }
}
