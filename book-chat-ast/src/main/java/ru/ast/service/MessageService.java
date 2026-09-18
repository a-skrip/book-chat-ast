package ru.ast.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ast.dto.MessageDto;
import ru.ast.entity.Book;
import ru.ast.entity.Character;
import ru.ast.entity.Chat;
import ru.ast.entity.Message;
import ru.ast.enums.MessageRole;
import ru.ast.mapper.MessagesMapper;
import ru.ast.repository.ChatRepository;
import ru.ast.repository.MessageRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final ModelChatService modelService;

    @Transactional
    protected Message sendQuestionAndSaveAnswer(Chat chat, String message, Book book, Character character) {
        Message answer = new Message();
//
        log.info("Получение истории");
        List<MessageDto> chatHistoryForModel = getChatHistoryForModel(chat.getId());

        String answerFromModel = modelService.getAnswerFromModel(
                message,
                book,
                character,
                chatHistoryForModel
        );

        Message question = createMessage(chat, MessageRole.USER, message);
        messageRepository.save(question);

        answer = createMessage(chat, MessageRole.SYSTEM, answerFromModel);
        messageRepository.save(answer);

        chatRepository.save(chat);
        log.info("Сохранены вопрос: {} | ответ: {} для chatId: {}",
                question.getText(),
                answer.getText(),
                chat.getId());
        return answer;
    }

    public List<MessageDto> getChatHistory(UUID chatId) {
        log.info("Получение истории для chatId: {}", chatId);
        List<Message> all = messageRepository.findAllByChatIdOrderByCreatedAtAsc(chatId);
        return MessagesMapper.toDtoList(all);
    }

    private Message createMessage(Chat chat, MessageRole role, String text) {
        Message message = new Message();
        message.setChat(chat);
        message.setMessageRole(role);
        message.setCreatedAt(LocalDateTime.now());
        message.setText(text);
        return message;
    }

    private List<MessageDto> getChatHistoryForModel(UUID chatId) {
        List<Message> history = messageRepository
                .findTop7ByChatIdOrderByCreatedAtDesc(chatId);
        List<Message> reversed = history.reversed();
        return MessagesMapper.toDtoList(reversed);
    }
}
