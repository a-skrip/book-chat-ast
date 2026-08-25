package ru.ast.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ast.dto.MessageDto;
import ru.ast.dto.request.ChatRequestNewDto;
import ru.ast.dto.response.ChatResponseDto;
import ru.ast.entity.*;
import ru.ast.entity.Character;
import ru.ast.exceptions.BookNotFoundException;
import ru.ast.exceptions.CharacterNotFoundException;
import ru.ast.exceptions.ChatNotFoundException;
import ru.ast.exceptions.SessionNotFoundException;
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
    private final MessageRepository messageRepository;
    private final ReaderSessionRepository sessionRepository;
    private final ReaderRepository readerRepository;
    private final SessionService sessionService;
    private final MessageService messageService;
    private final CharacterService characterService;
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
            throw new RuntimeException("Персонаж не принадлежит этой книге");
        }

        if (request.getSessionId() != null) {
            log.info("Найдена сессия: {},", request.getSessionId());
            ReaderSession session = sessionRepository.findById(UUID.fromString(request.getSessionId()))
                    .orElseThrow(() -> new SessionNotFoundException(UUID.fromString(request.getSessionId())));

            chat = findOrCreateChat(session, character);

            answer = sendMessage(chat, request.getMessage(), book.getId(), character.getName());

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
            answer = sendMessage(chat, request.getMessage(), book.getId(), character.getName());
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

            answer = sendMessage(chat, request.getMessage(), book.getId(), character.getName());

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

    private String sendMessage(Chat chat, String message, UUID bookId, String characterName) {
        Message result = messageService.sendQuestionAndSaveAnswer(chat, message, bookId, characterName);
        return result.getText();
    }

//    public SessionResponse startSession(UUID bookId,
//                                        HttpServletRequest req,
//                                        HttpServletResponse resp) {
//
//        SessionResponse response = new SessionResponse();
//
//        ReaderSession readerSession = sessionService.getReaderSession(bookId, req, resp);
//
//        List<Character> characters = characterRepository.findAllByBookId(bookId);
//        List<Chat> chatsBySessionId = chatRepository.findChatsBySessionId(readerSession.getId());
//        Book book = bookRepository.findById(bookId)
//                .orElseThrow(BookNotFoundException::new);
//
//        response.setSessionId(readerSession.getId().toString());
//        response.setBookId(bookId.toString());
//        response.setReaderId(readerSession.getReader().getId().toString());
//        response.setBookTitle(book.getTitle());
//        response.setCharacters(CharacterMapper.toDtoList(characters));
//        response.setExistingChats(ChatMapper.toDtoList(chatsBySessionId));
//
//        return response;
//    }
//
//
//    public ChatWithMessageResponseDto startChat(ChatRequestDto request) {
//        // 1. Проверяем все сущности
//        Book book = bookRepository.findById(request.bookId())
//                .orElseThrow(() -> new BookNotFoundException(request.bookId()));
//
//        Reader reader = readerRepository.findById(request.readerId())
//                .orElseThrow(() -> new ReaderNotFoundException(request.readerId()));
//
//        Character character = characterRepository.findById(request.characterId())
//                .orElseThrow(() -> new CharacterNotFoundException(request.characterId()));
//
//        ReaderSession session = sessionRepository.findById(request.sessionId())
//                .orElseThrow(() -> new SessionNotFoundException(request.sessionId()));
//
//        // 2. Проверяем, что персонаж принадлежит книге
//        if (!character.getBook().getId().equals(request.bookId())) {
//            throw new RuntimeException("Персонаж не принадлежит этой книге");
//        }
//        // 3. Ищем или создаём чат с этим персонажем
//        Chat chat = findOrCreateChat(session, character);
//
//        Message message = messageService.sendQuestionAndSaveAnswer(chat,
//                request.message(),
//                request.bookId(),
//                character.getName());
//
//        ChatWithMessageResponseDto response = new ChatWithMessageResponseDto();
//        response.setBookId(book.getId().toString());
//        response.setTitle(book.getTitle());
//        response.setCharacterName(character.getName());
//        response.setModel("Mistral");
//        response.setReply(message.getText());
//        List<MessageDto> chatHistory = messageService.getChatHistory(chat.getId());
//        response.setCanonChunks(chatHistory);
//        response.setCanonSufficient(true);
//
//        log.info("✅ Чат {} с персонажем {} продолжен", chat.getId(), character.getName());
//        return response;
//    }
//
//
//    public ReaderSessionResponseDto getSessionInfo(UUID sessionId) {
//        ReaderSession session = sessionRepository.findById(sessionId)
//                .orElseThrow(() -> new SessionNotFoundException(sessionId));
//
//        ReaderSessionResponseDto response = new ReaderSessionResponseDto();
//        response.setSessionId(session.getId());
//
//        List<Chat> chats = session.getChats();
//        log.info("Формирование списка чатов для session: {}", sessionId);
//
//        List<MessageDto> messageDtoList = new ArrayList<>();
//        for (Chat chat : chats) {
//            Message message = messageRepository.findFirstMessageFromReader(chat.getId());
//            MessageDto dto = MessagesMapper.toDto(message);
//
//            messageDtoList.add(dto);
//        }
//        response.setChats(messageDtoList);
//        return response;
//    }


}
