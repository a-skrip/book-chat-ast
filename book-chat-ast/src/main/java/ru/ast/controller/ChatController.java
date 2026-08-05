package ru.ast.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ast.dto.ChatRequestDto;
import ru.ast.dto.ChatWithMessageDto;
import ru.ast.dto.ReaderSessionInfoDto;
import ru.ast.service.ChatService;

import java.util.UUID;

@Slf4j

@RestController
@AllArgsConstructor
@RequestMapping("/chats")
public class ChatController {

    private final ChatService chatService;

    @PostMapping()
    public ChatWithMessageDto startChat(@RequestBody ChatRequestDto chatRequestDto) {
        return chatService.startChat(chatRequestDto);
    }

    @GetMapping()
    public ResponseEntity<ReaderSessionInfoDto> getLastChats(@RequestParam UUID sessionId) {
        ReaderSessionInfoDto session = chatService.getSessionInfo(sessionId);
        return ResponseEntity.ok(session);
    }

}
