package ru.ast.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ast.dto.MessageDto;
import ru.ast.dto.request.ChatRequestNewDto;
import ru.ast.dto.response.ChatResponseDto;
import ru.ast.entity.*;
import ru.ast.entity.Character;
import ru.ast.exceptions.*;
import ru.ast.repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class ChatService {

    private final BookRepository bookRepository;
    private final CharacterRepository characterRepository;
    private final ChatRepository chatRepository;
    private final ReaderSessionRepository sessionRepository;
    private final MessageService messageService;
    private final ReaderService readerService;


    public ChatResponseDto startOrContinueChat(ChatRequestNewDto request) {
        ChatResponseDto response = new ChatResponseDto();
        Chat chat;
        String answer;

        if (request.getBookId() == null ||
                request.getCharacterId() == null ||
                (request.getMessage() == null || request.getMessage().isEmpty())) {
            throw new IllegalArgumentException("Не переданы обязательные значения");
        }

        Book book = bookRepository.findById(UUID.fromString(request.getBookId()))
                .orElseThrow(() -> new BookNotFoundException(UUID.fromString(request.getBookId())));
        Character character = characterRepository.findById(UUID.fromString(request.getCharacterId()))
                .orElseThrow(() -> new CharacterNotFoundException(UUID.fromString(request.getCharacterId())));

        if (!character.getBook().getId().equals(book.getId())) {
            throw new CharacterNotBelongThisBookException("Персонаж не принадлежит этой книге");
        }

        if (request.getSessionId() != null) {
            log.info("Найдена сессия: {},", request.getSessionId());
            ReaderSession session = sessionRepository.findById(UUID.fromString(request.getSessionId()))
                    .orElseThrow(() -> new SessionNotFoundException(UUID.fromString(request.getSessionId())));

            chat = findOrCreateChat(session, character);

            answer = sendMessage(chat, request.getMessage(), book, character);

            List<MessageDto> chatHistory = messageService.getChatHistory(chat.getId());

            response.setConversationId(String.valueOf(chat.getId()));
            response.setSessionId(session.getId().toString());
            response.setBookId(String.valueOf(book.getId()));
            response.setBookTitle(book.getTitle());
            response.setCharacterName(character.getName());
            response.setReply(answer);
            response.setMessages(chatHistory);

            return response;

        }

        if (request.getConversationId() != null) {
            chat = chatRepository.findById(UUID.fromString(request.getConversationId()))
                    .orElseThrow(() -> new ChatNotFoundException(UUID.fromString(request.getConversationId())));
            log.info("Найден диалог: {} - продолжаем", request.getConversationId());
            UUID sessionId = chat.getSession().getId();
            UUID characterId = chat.getCharacter().getId();

            if (!characterId.equals(UUID.fromString(request.getCharacterId()))) {
                log.warn("Диалог принадлежит: {}, но передан {}", character.getName(), chat.getCharacter().getName());
                throw new IllegalArgumentException("Диалог ведется с другим персонажем");
            }
            answer = sendMessage(chat, request.getMessage(), book, character);
            List<MessageDto> chatHistory = messageService.getChatHistory(chat.getId());

            response.setConversationId(String.valueOf(chat.getId()));
            response.setSessionId(sessionId.toString());
            response.setBookId(String.valueOf(book.getId()));
            response.setBookTitle(book.getTitle());
            response.setCharacterName(character.getName());
            response.setReply(answer);
            response.setMessages(chatHistory);

            return response;
        }

        if (request.getSessionId() == null && request.getConversationId() == null) {
            ReaderSession session = new ReaderSession();
            Reader reader = readerService.createReader();
            session.setBook(book);
            session.setReader(reader);

            ReaderSession saved = sessionRepository.save(session);
            log.info("Создана сессия: {}", saved.getId());
            chat = new Chat();

            chat.setSession(saved);
            chat.setCharacter(character);

            chat = chatRepository.save(chat);
            log.info("Создан чат: {}, по книге: {}, персонаж: {}, читатель: {}",
                    chat.getId(), book.getTitle(), character.getName(), reader.getName());

            answer = sendMessage(chat, request.getMessage(), book, character);

            List<MessageDto> chatHistory = messageService.getChatHistory(chat.getId());

            response.setConversationId(String.valueOf(chat.getId()));
            response.setSessionId(saved.getId().toString());
            response.setBookId(String.valueOf(book.getId()));
            response.setBookTitle(book.getTitle());
            response.setCharacterName(character.getName());
            response.setReply(answer);
            response.setMessages(chatHistory);

            return response;
        }
        return response;
    }

    public ChatResponseDto createNewChat(String bookId, String characterId) {

        Book book = bookRepository.findById(UUID.fromString(bookId))
                .orElseThrow(() -> new BookNotFoundException(UUID.fromString(bookId)));
        Character character = characterRepository.findById(UUID.fromString(characterId))
                .orElseThrow(() -> new CharacterNotFoundException(UUID.fromString(characterId)));

        if (!character.getBook().getId().equals(book.getId())) {
            throw new CharacterNotBelongThisBookException("Персонаж не принадлежит этой книге");
        }

        ChatResponseDto response = new ChatResponseDto();
        Reader reader = readerService.createReader();


        ReaderSession session = new ReaderSession();
        Chat chat = new Chat();

        session.setBook(book);
        session.setReader(reader);

        chat.setCharacter(character);
        chat.setSession(session);

        session.setChats(List.of(chat));

        ReaderSession savedSession = sessionRepository.save(session);
        Chat savedChat = chatRepository.save(chat);

        log.info("Новая сессия: {} для нового пользователя: {} чат с персонажем: {}",
                savedSession.getId(), reader.getName(), savedChat.getCharacter().getName());

        response.setConversationId(savedChat.getId().toString());
        response.setSessionId(savedSession.getId().toString());
        response.setBookId(bookId);
        response.setBookTitle(book.getTitle());
        response.setCharacterName(character.getName());
        response.setMessages(new ArrayList<>());

        return response;
    }

    private Chat findOrCreateChat(ReaderSession session, Character character) {
        // Ищем существующий чат с этим персонажем
        log.info("Поиск существующих чатов");
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

    private String sendMessage(Chat chat, String message, Book book, Character character) {
        Message result = messageService.sendQuestionAndSaveAnswer(chat, message,book, character);
        return result.getText();
    }
}
