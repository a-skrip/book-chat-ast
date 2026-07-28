package ru.ast.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.ast.dto.CharacterDto;
import ru.ast.service.CharacterExtractionService;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class CharacterFindController {

    private final CharacterExtractionService characterFindService;

    @GetMapping("/char")
    public List<CharacterDto> findCharacters(@RequestParam UUID bookId) {
        return characterFindService.findCharacters(bookId);
    }
}
