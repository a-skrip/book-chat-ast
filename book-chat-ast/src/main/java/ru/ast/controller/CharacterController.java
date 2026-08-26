package ru.ast.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.ast.dto.CharacterDto;
import ru.ast.dto.response.CharactersResponseDto;
import ru.ast.dto.request.CharacterRequestDto;
import ru.ast.service.CharacterService;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
@Tag(name = "Character", description = "Api для управления персонажами")
@SecurityRequirement(name = "bearerAuth")
public class CharacterController {

    private final CharacterService characterService;

    @Operation(summary = "Получить всех персонажей книги")
    @GetMapping("/books/{bookId}/characters")
    public ResponseEntity<CharactersResponseDto> getCharacters(@PathVariable UUID bookId) {
        return ResponseEntity.ok(characterService.getAllCharactersForBook(bookId));
    }

    @Operation(summary = "Получить персонажа по ID")
    @GetMapping("/characters/{id}")
    public ResponseEntity<CharacterDto> getCharacter(@PathVariable UUID id) {
        return ResponseEntity.ok(characterService.getCharacter(id));
    }

    @Operation(
            summary = "Извлекает персонажей из книги",
            description = "Извлекает персонажей из произведения и сохраняет в БД используя LLM-модель"
    )
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/books/{bookId}/characters/extract")
    public ResponseEntity<CharactersResponseDto> extractCharacters(@PathVariable UUID bookId) {
        return ResponseEntity.ok(characterService.extractCharacters(bookId));
    }

    @Operation(summary = "Изменение персонажа",
            description = "Только для админов (нужен JWT)")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PatchMapping("/characters/{characterId}")
    public ResponseEntity<CharacterDto> updateCharacter(
            @PathVariable UUID characterId,
            @RequestBody CharacterRequestDto request) {
        return ResponseEntity.ok(characterService.updateCharacter(characterId, request));
    }

}
