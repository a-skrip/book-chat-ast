package ru.ast.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ast.dto.ChatRequestDto;
import ru.ast.dto.ChatWithMessageDto;
import ru.ast.dto.MessageDto;
import ru.ast.entity.*;
import ru.ast.entity.Character;
import ru.ast.enums.MessageRole;
import ru.ast.exceptions.BookNotFoundException;
import ru.ast.exceptions.CharacterNotFoundException;
import ru.ast.exceptions.CharactersForBookNotExistException;
import ru.ast.exceptions.ReaderNotFoundException;
import ru.ast.repository.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class ChatService {

    private final ModelService modelService;
    private final BookRepository bookRepository;
    private final CharacterRepository characterRepository;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final ReaderSessionRepository sessionRepository;
    private final ReaderRepository readerRepository;


    public ChatWithMessageDto startChat(ChatRequestDto request) {

        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new BookNotFoundException(request.bookId()));

        Reader reader = readerRepository.findById(request.readerId())
                .orElseThrow(() -> new ReaderNotFoundException(request.readerId()));

        if (!characterRepository.existsCharactersByBookId(request.bookId())) {
            log.warn("Нет героев для книги: {}",  request.bookId());
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

        Chat chat = chatRepository.findChatBySessionIdAndCharacterId(session.getId(), character.getId())
                .orElseGet(() -> createChat(character, session));

        Message question = createMessage(chat, MessageRole.USER, request.message());
        messageRepository.save(question);

        String answerFromModel = modelService.getAnswerFromModel(request.message(), book.getId(), character.getName());
        Message answer = createMessage(chat, MessageRole.SYSTEM, answerFromModel);
        messageRepository.save(answer);

        ChatWithMessageDto response = new ChatWithMessageDto();
        response.setChatId(chat.getId().toString());
        response.setCharacterId(character.getId().toString());

        List<Message> chatHistory = getChatHistory(chat.getId());
        List<MessageDto> collect = chatHistory.stream()
                .map(el -> {
                    String role = el.getMessageRole().toString();
                    String text = el.getText();
                    LocalDateTime createdAt = el.getCreatedAt();
                    MessageDto dto = new MessageDto();
                    dto.setRole(role);
                    dto.setMessage(text);
                    dto.setTimestamp(createdAt);
                    return dto;
                })
                .toList();

        response.setMessages(collect);

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

    private List<Message> getChatHistory(UUID chatId) {
        return messageRepository.findAllByChatId(chatId);
    }

}
