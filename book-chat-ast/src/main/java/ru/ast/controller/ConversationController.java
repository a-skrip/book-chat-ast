package ru.ast.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ast.dto.response.ConversationResponseDto;
import ru.ast.service.SessionService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Tag(name = "Conversation", description = "API для получения переписки")
public class ConversationController {

    private final SessionService sessionService;

    @Operation(summary = "Получение списка диалогов по книге")
    @GetMapping("/books/{bookId}/conversations")
    public ResponseEntity<ConversationResponseDto> getDialogsForBook(@PathVariable UUID bookId) {
        ConversationResponseDto response = sessionService.getDialogsForBook(bookId);
        return ResponseEntity.status(200).body(response);
    }

    @Operation(summary = "Получение диалога с персонажем по книге")
    @GetMapping("books/{bookId}/characters/{characterId}/conversations")
    public ResponseEntity<ConversationResponseDto> getDialogForBookWitCharacter(
            @PathVariable UUID bookId,
            @PathVariable UUID characterId) {
        ConversationResponseDto response = sessionService.getDialogWitCharacter(bookId, characterId);
        return ResponseEntity.status(200).body(response);
    }

    @Operation(summary = "Получение диалога по ID")
    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ConversationResponseDto> getConversationById(@PathVariable UUID conversationId) {
        ConversationResponseDto response = sessionService.getDialogById(conversationId);
        return ResponseEntity.ok(response);
    }


}
