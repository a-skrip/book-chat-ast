package ru.ast.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ast.dto.request.ChatRequestNewDto;
import ru.ast.dto.response.ChatResponseDto;
import ru.ast.service.ChatService;
import ru.ast.service.MessageService;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api")
@Tag(name = "Chat", description = "API для ведения диалога")
public class ChatController {

    private final ChatService chatService;
    private final MessageService messageService;

    @Operation(summary = "Отправляет сообщение моделе для получения ответа")
    @PostMapping("/chat/respond")
    public ResponseEntity<ChatResponseDto> startDialog(@RequestBody ChatRequestNewDto request) {
        ChatResponseDto response = chatService.startOrContinueChat(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Создание нового диалога по книге с выбранным персонажем")
    @PostMapping("books/{bookId}/characters/{characterId}/chat/new")
    public ResponseEntity<ChatResponseDto> newChat(
            @PathVariable("bookId") String bookId,
            @PathVariable("characterId") String characterId) {
        ChatResponseDto response = chatService.createNewChat(bookId, characterId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
