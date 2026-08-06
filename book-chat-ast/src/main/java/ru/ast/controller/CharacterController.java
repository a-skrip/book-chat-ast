package ru.ast.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.ast.dto.CharacterDto;
import ru.ast.service.CharacterService;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class CharacterController {

    private final CharacterService characterService;

    @PostMapping("/characters")
    public List<CharacterDto> findCharacters(@RequestParam UUID bookId) {
        return characterService.findCharacters(bookId);
    }
}
