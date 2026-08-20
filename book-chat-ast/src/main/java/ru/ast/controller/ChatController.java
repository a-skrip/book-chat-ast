package ru.ast.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ast.dto.request.ChatRequestDto;
import ru.ast.dto.response.ChatWithMessageResponseDto;
import ru.ast.dto.response.ReaderSessionResponseDto;
import ru.ast.dto.response.SessionResponse;
import ru.ast.service.ChatService;
import ru.ast.service.MessageService;

import java.util.UUID;

@Slf4j

@RestController
@AllArgsConstructor
@RequestMapping("/chats")
public class ChatController {

    private final ChatService chatService;
    private final MessageService messageService;

    @PostMapping()
    public ChatWithMessageResponseDto startChat(@RequestBody ChatRequestDto chatRequestDto) {
        return chatService.startChat(chatRequestDto);
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<ReaderSessionResponseDto> getLastChats(@PathVariable UUID sessionId) {
        ReaderSessionResponseDto session = chatService.getSessionInfo(sessionId);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/books/{bookId}")
    public ResponseEntity<SessionResponse> enterBookSession(
            @PathVariable UUID bookId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        SessionResponse result = chatService.startSession(bookId, request, response);
        return ResponseEntity.ok(result);
    }
}
