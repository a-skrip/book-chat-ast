package ru.ast.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ast.dto.request.ChatRequestDto;
import ru.ast.dto.response.ChatWithMessageResponseDto;
import ru.ast.dto.response.ReaderSessionResponseDto;
import ru.ast.service.ChatService;

import java.util.UUID;

@Slf4j

@RestController
@AllArgsConstructor
@RequestMapping("/chats")
public class ChatController {

    private final ChatService chatService;

    @PostMapping()
    public ChatWithMessageResponseDto startChat(@RequestBody ChatRequestDto chatRequestDto) {
        return chatService.startChat(chatRequestDto);
    }

    @GetMapping()
    public ResponseEntity<ReaderSessionResponseDto> getLastChats(@RequestParam UUID sessionId) {
        ReaderSessionResponseDto session = chatService.getSessionInfo(sessionId);
        return ResponseEntity.ok(session);
    }

}
