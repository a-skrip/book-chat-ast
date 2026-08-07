package ru.ast.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ast.dto.request.ChatRequestDto;
import ru.ast.dto.response.ChatWithMessageResponseDto;
import ru.ast.dto.MessageDto;
import ru.ast.dto.response.ReaderSessionResponseDto;
import ru.ast.entity.*;
import ru.ast.entity.Character;
import ru.ast.enums.MessageRole;
import ru.ast.exceptions.*;
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


    public ChatWithMessageResponseDto startChat(ChatRequestDto request) {

        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new BookNotFoundException(request.bookId()));

        Reader reader = readerRepository.findById(request.readerId())
                .orElseThrow(() -> new ReaderNotFoundException(request.readerId()));

        if (!characterRepository.existsCharactersByBookId(request.bookId())) {
            log.warn("Нет героев для книги: {}", request.bookId());
            throw new CharactersForBookNotExistException(request.bookId());
        }
        Character character = characterRepository.findById(request.characterId())
                .orElseThrow(() -> new CharacterNotFoundException(request.characterId()));

        log.info("Начинаем чат с читателем: {} по книге: \"{}\" с героем: {}",
                reader.getName(),
                book.getTitle(),
                character.getName());

        ReaderSession session = sessionRepository.findReaderSessionByReaderId(reader.getId())
                .orElseGet(() -> createReaderSession(reader, book));

        //TODO при подмене book_id продолжается переписка в том же чате, а должна открыться новая сессия
        Chat chat = chatRepository.findChatBySessionIdAndCharacterId(session.getId(), character.getId())
                .orElseGet(() -> createChat(character, session));

        Message question = createMessage(chat, MessageRole.USER, request.message());
        messageRepository.save(question);

        List<MessageDto> chatHistoryForModel = getChatHistoryForModel(chat.getId());

        String answerFromModel = modelService.getAnswerFromModel(
                request.message(),
                book.getId(),
                character.getName(),
                chatHistoryForModel
        );

        Message answer = createMessage(chat, MessageRole.SYSTEM, answerFromModel);
        messageRepository.save(answer);

        ChatWithMessageResponseDto response = new ChatWithMessageResponseDto();
        response.setChatId(chat.getId().toString());
        response.setTitle(book.getTitle());
        response.setCharacterName(character.getName());
        response.setReaderId(reader.getId().toString());
        response.setReaderSession(session.getId().toString());

        List<MessageDto> chatHistory = getChatHistory(chat.getId());
        response.setMessages(chatHistory);

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

    private ReaderSession createReaderSession(Reader reader, Book book) {
        ReaderSession readerSession = new ReaderSession();
        readerSession.setReader(reader);
        readerSession.setBook(book);
        readerSession.setChats(new ArrayList<>());

        return sessionRepository.save(readerSession);
    }

    private Chat createChat(Character character, ReaderSession readerSession) {
        Chat chat = new Chat();
        chat.setCharacter(character);
        chat.setSession(readerSession);
        chat.setMessages(new ArrayList<>());

        return chatRepository.save(chat);
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
                .findTop7ByChatIdOrderByCreatedAtDesc(chatId);
        List<Message> reversed = history.reversed();
        return MessagesMapper.toDtoList(reversed);
    }
}
