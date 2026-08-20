package ru.ast.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.ast.dto.MessageDto;
import ru.ast.dto.response.ConversationDto;
import ru.ast.service.MessageService;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class ConversationController {

    private final MessageService messageService;

    @GetMapping("/conversations/{conversation_id}")
    public ResponseEntity<List<ConversationDto>> getConversations(@PathVariable UUID conversation_id) {
        List<MessageDto> chatHistory = messageService.getChatHistory(conversation_id);
        List<ConversationDto> list = chatHistory.stream()
                .map(elem -> {
                    ConversationDto conversationDto = new ConversationDto();
                    conversationDto.setRole(elem.getRole());
                    conversationDto.setMessage(elem.getMessage());

                    return conversationDto;
                })
                .toList();
        return ResponseEntity.status(200).body(list);
    }
}
