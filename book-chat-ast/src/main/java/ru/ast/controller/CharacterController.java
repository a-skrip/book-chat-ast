package ru.ast.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.ast.dto.CharacterRequestDto;
import ru.ast.dto.CharacterResponseDto;
import ru.ast.dto.CharactersResponseDto;
import ru.ast.service.CharacterService;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class CharacterController {

    private final CharacterService characterService;

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/books/{bookId}/characters/extract")
    public ResponseEntity<CharactersResponseDto> extractCharacters(@PathVariable UUID bookId) {
        return ResponseEntity.ok(characterService.extractCharacters(bookId));
    }

    @GetMapping("/books/{bookId}/characters")
    public ResponseEntity<CharactersResponseDto> getCharacters(@PathVariable UUID bookId) {
        return ResponseEntity.ok(characterService.getAllCharactersForBook(bookId));
    }
    @GetMapping("/characters/{id}")
    public ResponseEntity<CharacterResponseDto> getCharacter(@PathVariable UUID id) {
        return ResponseEntity.ok(characterService.getCharacter(id));
    }

    @PatchMapping("/characters/{characterId}")
    public ResponseEntity<CharacterResponseDto> updateCharacter(
            @PathVariable UUID characterId,
            @RequestBody CharacterRequestDto request) {
        return ResponseEntity.ok(characterService.updateCharacter(characterId, request));
    }

}
