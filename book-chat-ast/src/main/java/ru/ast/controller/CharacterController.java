package ru.ast.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.ast.dto.CharacterResponseDto;
import ru.ast.dto.CharactersResponseDto;
import ru.ast.dto.request.CharacterRequestDto;
import ru.ast.service.CharacterService;

import java.util.UUID;

@RestController
@AllArgsConstructor
@Tag(name = "Character", description = "Api для управления персонажами")
public class CharacterController {

    private final CharacterService characterService;

    @Operation(
         summary = "Извлекает персонажей из книги",
            description = "Извлекает персонажей из произведения и сохраняет в БД используя LLM-модель"
    )

    @PreAuthorize("hasAnyRole('ADMIN')" )
    @PostMapping("/books/{bookId}/characters/extract" )
    public ResponseEntity<CharactersResponseDto> extractCharacters(@PathVariable UUID bookId) {
        return ResponseEntity.ok(characterService.extractCharacters(bookId));
    }

    @GetMapping("/books/{bookId}/characters" )
    public ResponseEntity<CharactersResponseDto> getCharacters(@PathVariable UUID bookId) {
        return ResponseEntity.ok(characterService.getAllCharactersForBook(bookId));
    }

    @GetMapping("/characters/{id}" )
    public ResponseEntity<CharacterResponseDto> getCharacter(@PathVariable UUID id) {
        return ResponseEntity.ok(characterService.getCharacter(id));
    }

    @PatchMapping("/characters/{characterId}" )
    public ResponseEntity<CharacterResponseDto> updateCharacter(
            @PathVariable UUID characterId,
            @RequestBody CharacterRequestDto request) {
        return ResponseEntity.ok(characterService.updateCharacter(characterId, request));
    }

}
