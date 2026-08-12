package ru.ast.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ast.dto.MessageDto;
import ru.ast.dto.request.ChatRequestDto;
import ru.ast.dto.response.ChatWithMessageResponseDto;
import ru.ast.dto.response.ReaderSessionResponseDto;
import ru.ast.dto.response.SessionResponse;
import ru.ast.entity.*;
import ru.ast.entity.Character;
import ru.ast.enums.MessageRole;
import ru.ast.exceptions.BookNotFoundException;
import ru.ast.exceptions.CharacterNotFoundException;
import ru.ast.exceptions.ReaderNotFoundException;
import ru.ast.exceptions.SessionNotFoundException;
import ru.ast.mapper.CharacterMapper;
import ru.ast.mapper.ChatMapper;
import ru.ast.mapper.MessagesMapper;
import ru.ast.repository.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class ChatService {

    private final ModelChatService modelService;
    private final BookRepository bookRepository;
    private final CharacterRepository characterRepository;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final ReaderSessionRepository sessionRepository;
    private final ReaderRepository readerRepository;
    private final SessionService sessionService;


    public SessionResponse startSession(UUID bookId,
                                        HttpServletRequest req,
                                        HttpServletResponse resp) {

        SessionResponse response = new SessionResponse();

        ReaderSession readerSession = sessionService.getReaderSession(bookId, req, resp);

        List<Character> characters = characterRepository.findAllByBookId(bookId);
        List<Chat> chatsBySessionId = chatRepository.findChatsBySessionId(readerSession.getId());
        Book book = bookRepository.findById(bookId)
                .orElseThrow(BookNotFoundException::new);


        response.setSessionId(readerSession.getId().toString());
        response.setBookId(bookId.toString());
        response.setReaderId(readerSession.getId().toString());
        response.setBookTitle(book.getTitle());
        response.setCharacters(CharacterMapper.toDtoList(characters));
        response.setExistingChats(ChatMapper.toDtoList(chatsBySessionId));

        return response;

    }


    public ChatWithMessageResponseDto startChat(ChatRequestDto request) {
        // 1. Проверяем все сущности
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new BookNotFoundException(request.bookId()));

        Reader reader = readerRepository.findById(request.readerId())
                .orElseThrow(() -> new ReaderNotFoundException(request.readerId()));

        Character character = characterRepository.findById(request.characterId())
                .orElseThrow(() -> new CharacterNotFoundException(request.characterId()));

        ReaderSession session = sessionRepository.findById(request.sessionId())
                .orElseThrow(() -> new SessionNotFoundException(request.sessionId()));

        // 2. Проверяем, что персонаж принадлежит книге
        if (!character.getBook().getId().equals(request.bookId())) {
            throw new RuntimeException("Персонаж не принадлежит этой книге");
        }
        // 3. Ищем или создаём чат с этим персонажем
        Chat chat = findOrCreateChat(session, character);

        sendQuestionAndSaveAnswer(chat, request.message(), request.bookId(), character.getName());

        ChatWithMessageResponseDto response = new ChatWithMessageResponseDto();
        response.setBookId(book.getId().toString());
        response.setTitle(book.getTitle());
        response.setCharacterName(character.getName());
        response.setReaderId(reader.getId().toString());
        response.setReaderSession(session.getId().toString());
        response.setChatId(chat.getId().toString());
        response.setMessages(getChatHistory(chat.getId()));

        log.info("✅ Чат {} с персонажем {} продолжен", chat.getId(), character.getName());
        return response;
    }


    public ReaderSessionResponseDto getSessionInfo(UUID sessionId) {
        ReaderSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        ReaderSessionResponseDto response = new ReaderSessionResponseDto();
        response.setSessionId(session.getId());

        List<Chat> chats = session.getChats();
        log.info("Формирование списка чатов для session: {}", sessionId);

        List<MessageDto> messageDtoList = new ArrayList<>();
        for (Chat chat : chats) {
            Message message = messageRepository.findFirstMessageFromReader(chat.getId());
            MessageDto dto = MessagesMapper.toDto(message);

            messageDtoList.add(dto);
        }
        response.setChats(messageDtoList);
        return response;
    }

    private Message createMessage(Chat chat, MessageRole role, String text) {
        Message message = new Message();
        message.setChat(chat);
        message.setMessageRole(role);
        message.setCreatedAt(LocalDateTime.now());
        message.setText(text);
        return message;
    }

    private List<MessageDto> getChatHistory(UUID chatId) {
        return MessagesMapper.toDtoList(messageRepository.findAllByChatIdOrderByCreatedAtAsc(chatId));
    }

    private List<MessageDto> getChatHistoryForModel(UUID chatId) {
        List<Message> history = messageRepository
                .findTop5ByChatIdOrderByCreatedAtDesc(chatId);
        List<Message> reversed = history.reversed();
        return MessagesMapper.toDtoList(reversed);
    }

    private void sendQuestionAndSaveAnswer(Chat chat, String message, UUID bookId, String characterName) {
        Message question = createMessage(chat, MessageRole.USER, message);
        messageRepository.save(question);

        List<MessageDto> chatHistoryForModel = getChatHistoryForModel(chat.getId());

        String answerFromModel = modelService.getAnswerFromModel(
                message,
                bookId,
                characterName,
                chatHistoryForModel
        );

        Message answer = createMessage(chat, MessageRole.SYSTEM, answerFromModel);
        messageRepository.save(answer);
    }

    private Chat findOrCreateChat(ReaderSession session, Character character) {
        // Ищем существующий чат с этим персонажем
        return chatRepository.findChatBySessionIdAndCharacterId(session.getId(), character.getId())
                .orElseGet(() -> {
                    log.info("🆕 Создаём новый чат с персонажем: {}", character.getName());
                    Chat newChat = new Chat();
                    newChat.setCharacter(character);
                    newChat.setSession(session);
                    newChat.setMessages(new ArrayList<>());

                    return chatRepository.save(newChat);
                });
    }
}
