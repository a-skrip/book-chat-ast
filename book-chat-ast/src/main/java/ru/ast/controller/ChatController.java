package ru.ast.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ast.dto.ChatRequestDto;
import ru.ast.dto.ChatWithMessageDto;
import ru.ast.dto.ReaderSessionDto;
import ru.ast.service.ChatService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/chats")
public class ChatController {

    private final ChatService chatService;

    @PostMapping()
    public ChatWithMessageDto test(@RequestBody ChatRequestDto chatRequestDto) {
        return chatService.startChat(chatRequestDto);
    }

    @GetMapping()
    public ResponseEntity<ReaderSessionDto> getLastChats(@RequestParam UUID sessionId) {
        ReaderSessionDto session = chatService.getSession(sessionId);
        return ResponseEntity.ok(session);
    }
}
