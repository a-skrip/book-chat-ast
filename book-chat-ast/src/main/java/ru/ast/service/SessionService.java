package ru.ast.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ast.entity.Book;
import ru.ast.entity.Reader;
import ru.ast.entity.ReaderSession;
import ru.ast.exceptions.BookNotFoundException;
import ru.ast.repository.BookRepository;
import ru.ast.repository.ReaderRepository;
import ru.ast.repository.ReaderSessionRepository;

import java.util.ArrayList;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class SessionService {

    private final ReaderSessionRepository sessionRepository;

    private final ReaderRepository readerRepository;
    private final BookRepository bookRepository;

    private static final String SESSION_COOKIE_NAME = "reader_session_id";
    private static final int COOKIE_MAX_AGE = 360; // в минутах
//    private static final int COOKIE_MAX_AGE = 60 * 60 * 24 * 365; // в минутах - 1 год


    public ReaderSession getReaderSession(UUID bookId,
                                          HttpServletRequest request,
                                          HttpServletResponse response) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(BookNotFoundException::new);

        // 1. Пытаемся найти sessionId в cookie
        String idFromCookie = getReaderSessionIdFromCookie(request);
        ReaderSession readerSession = null;

        if (idFromCookie != null) {
            readerSession = sessionRepository.findById(UUID.fromString(idFromCookie))
                    .orElse(null);
            if (readerSession != null) {

                if (readerSession.getBook().getId().equals(book.getId())) {
                    log.info("Найдена сессия: {}, для книги id: {}", readerSession.getId(), book.getId());
                    return sessionRepository.save(readerSession);
                }
            }
        }

        readerSession = createReaderSession(book.getId());
        setSessionCookie(response, readerSession.getId().toString());

        log.info("Создана новая сессия: {} для книги: {}", readerSession.getId(), book.getTitle());
        return readerSession;

    }

    private ReaderSession createReaderSession(UUID bookId) {
        ReaderSession readerSession = new ReaderSession();
        Book book = bookRepository.findById(bookId)
                .orElseThrow(BookNotFoundException::new);

        Reader reader = new Reader();
        Reader savedReader = readerRepository.save(reader);
        UUID readerId = savedReader.getId();
        reader.setName("reader_id_" + readerId);
        Reader saveWithName = readerRepository.saveAndFlush(reader);

        log.info("Создан новый читатель: {}, с id: {}", saveWithName.getName(), saveWithName.getId());

        readerSession.setBook(book);
        readerSession.setReader(savedReader);
        readerSession.setChats(new ArrayList<>());
        ReaderSession session = sessionRepository.save(readerSession);

        log.info("Создана сессия: {}, для пользователя: {}", session.getId(), session.getReader().getId());
        return session;
    }

    private boolean sessionExist(UUID sessionId) {
        return sessionRepository.findById(sessionId).isPresent();
    }


    private String getReaderSessionIdFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (SESSION_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void setSessionCookie(HttpServletResponse response, String sessionId) {
        Cookie cookie = new Cookie(SESSION_COOKIE_NAME, sessionId);
        cookie.setHttpOnly(true);      // Недоступен для JavaScript (безопасность)
        cookie.setSecure(false);       // true для HTTPS
        cookie.setPath("/");           // Доступен для всех эндпоинтов
        cookie.setMaxAge(COOKIE_MAX_AGE);
        log.info("Установка COOKIE для sessionId: {}, life = {} ", sessionId, COOKIE_MAX_AGE);
        response.addCookie(cookie);
    }

    public void logout(HttpServletResponse response) {
        Cookie cookie = new Cookie(SESSION_COOKIE_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Удаляем cookie
        response.addCookie(cookie);
        log.info("Сессия удалена");
    }


}
