package ru.ast.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ast.dto.MessageDto;
import ru.ast.entity.Chat;
import ru.ast.entity.Message;
import ru.ast.enums.MessageRole;
import ru.ast.mapper.MessagesMapper;
import ru.ast.repository.ChatRepository;
import ru.ast.repository.MessageRepository;
import ru.ast.util.MistralHealthIndicator;

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
    private  final MistralHealthIndicator healthIndicator;

    @Transactional
    protected void sendQuestionAndSaveAnswer(Chat chat, String message, UUID bookId, String characterName) {
        try {
            // 1. Получаем историю для модели (ДО сохранения вопроса)
            List<MessageDto> chatHistoryForModel = getChatHistoryForModel(chat.getId());
            // 2. Получаем ответ от модели
            String answerFromModel = modelService.getAnswerFromModel(
                    message,
                    bookId,
                    characterName,
                    chatHistoryForModel
            );
            // 3. ТОЛЬКО ПОСЛЕ успешного получения ответа — сохраняем оба сообщения
            Message question = createMessage(chat, MessageRole.USER, message);
            messageRepository.save(question);

            Message answer = createMessage(chat, MessageRole.SYSTEM, answerFromModel);
            messageRepository.save(answer);

            chatRepository.save(chat);
            log.info("Сохранены вопрос: {} | ответ: {} для chatId: {}",
                    question.getText(),
                    answer.getText(),
                    chat.getId());
        }  catch (Exception e) {
            log.error("❌ Ошибка при получении ответа от модели: {}", e.getMessage(), e);
            throw  new RuntimeException("Не удалось получить ответ от модели", e);
        }
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
    public List<MessageDto> getChatHistory(UUID chatId) {
        return MessagesMapper.toDtoList(messageRepository.findAllByChatIdOrderByCreatedAtAsc(chatId));
    }

}
