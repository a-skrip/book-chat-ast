package ru.ast.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import ru.ast.dto.MessageDto;
import ru.ast.entity.Book;
import ru.ast.entity.Character;
import ru.ast.enums.MessageRole;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ModelChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    // Ключевые слова для определения вопроса о книге
    private static final String[] BOOK_KEYWORDS = {
            "Бэла", "Печорин", "Максим Максимыч", "Казбич",
            "княжна Мери", "Грушницкий", "Вернер", "Вера",
            "роман", "книга", "глава", "часть", "сюжет",
            "герой", "персонаж", "история", "смерть", "любовь",
            "дуэль", "судьба", "характер", "поступок", "чувства"
    };


    public String getAnswerFromModel(String question, Book book, Character character, List<MessageDto> history) {
        long start = System.currentTimeMillis();
        log.info("Вопрос: '{}' | Книга: {} | Персонаж: {}",
                question, book.getTitle(), character.getName());

        // 1. Поиск чанков
        List<Document> chunks = findRelevantChunks(question, book.getId(), character.getName());
        log.info("Найдено чанков: {}", chunks.size());

        // 2. Формируем контекст и историю
        String context = buildContext(chunks);
        logContext(context);
        String historyText = buildHistoryText(history, character.getName());

        // 3. Определяем тип вопроса
        DialogMode mode = determineDialogMode(question, chunks, character.getName());
        log.info("🎯 Режим диалога: {}", mode);

        // 4. Формируем промпт
        PromptData promptData = buildPrompt(mode, question, book, character, context, historyText);

        // 5. Отправляем запрос
        String answer = chatClient.prompt()
                .system(promptData.system())
                .user(promptData.user())
                .call()
                .content();

        log.info("Модель ответила за: {} ms", System.currentTimeMillis() - start);
        log.info("Ответ: {}", answer);

        return answer;
    }

    // ==================== ОПРЕДЕЛЕНИЕ РЕЖИМА ====================

    private enum DialogMode {
        FREE_DIALOG,      // Вопрос не по книге
        BOOK_RAG,         // Вопрос по книге + есть контекст
        BOOK_FALLBACK     // Вопрос по книге + нет контекста
    }

    private DialogMode determineDialogMode(String question, List<Document> chunks, String character) {
        boolean isBookQuestion = isQuestionAboutBook(question);
        boolean hasContext = !chunks.isEmpty();
        boolean hasCharacterSpeech = hasContext && contextContainsCharacterSpeech(chunks, character);

        if (!isBookQuestion && !hasContext) {
            return DialogMode.FREE_DIALOG;
        } else if (isBookQuestion && hasContext && hasCharacterSpeech) {
            return DialogMode.BOOK_RAG;
        } else {
            return DialogMode.BOOK_FALLBACK;
        }
    }

    // ==================== ПРОВЕРКИ ====================

    private boolean isQuestionAboutBook(String question) {
        String lower = question.toLowerCase();
        for (String keyword : BOOK_KEYWORDS) {
            if (lower.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        // Дополнительная проверка: есть ли слово "книга" или "роман"
        return lower.contains("книг") || lower.contains("роман");
    }

    private boolean contextContainsCharacterSpeech(List<Document> chunks, String character) {
        for (Document chunk : chunks) {
            String text = chunk.getText();
            if (text.contains("— " + character) ||
                    text.contains(character + " сказал") ||
                    text.contains(character + " отвечал") ||
                    text.contains(character + " говорил") ||
                    text.contains(character + " спросил") ||
                    text.contains(character + " произнес")) {
                return true;
            }
        }
        return false;
    }

    // ==================== ПОСТРОЕНИЕ ПРОМПТА ====================

    private record PromptData(String system, String user) {
    }

    private PromptData buildPrompt(DialogMode mode, String question, Book book,
                                   Character character, String context, String historyText) {
        return switch (mode) {
            case FREE_DIALOG -> buildFreeDialogPrompt(question, book, character, historyText);
            case BOOK_RAG -> buildBookRagPrompt(question, book, character, context, historyText);
            case BOOK_FALLBACK -> buildBookFallbackPrompt(question, book, character, historyText);
        };
    }

    // Режим 1: Свободный диалог
    private PromptData buildFreeDialogPrompt(String question, Book book,
                                             Character character, String historyText) {
        String system = String.format("""
                        Ты — %s, персонаж романа «%s».
                        Сейчас читатель задал вопрос, не связанный с книгой.
                        
                        ПРАВИЛА:
                        1. Отвечай от лица %s.
                        2. Поддерживай естественный диалог.
                        3. Будь вежлив(а) и дружелюбен(на).
                        4. Отвечай кратко (1-2 предложения).
                        5. НЕ используй звёздочки, подчёркивания, скобки.
                        
                        ИСТОРИЯ ДИАЛОГА:
                        %s
                        """,
                character.getName(),
                book.getTitle(),
                character.getName(),
                historyText
        );

        String user = String.format("""
                        ВОПРОС:
                        %s
                        
                        ОТВЕТ ОТ ЛИЦА %s:
                        """,
                question,
                character.getName()
        );

        return new PromptData(system, user);
    }

    // Режим 2: Вопрос по книге (с контекстом)
    private PromptData buildBookRagPrompt(String question, Book book,
                                          Character character, String context, String historyText) {
        // Логируем контекст (обрезанный)
        logContext(context);

        String system = String.format("""
                        Ты — %s, персонаж романа «%s».
                        
                        ТВОЙ ХАРАКТЕР:
                        %s
                        
                        ПРАВИЛА:
                        1. Отвечай ТОЛЬКО от лица %s.
                        2. Используй ТОЛЬКО информацию из КОНТЕКСТА.
                        3. Если в контексте есть информация — используй её.
                        4. Если в контексте нет прямого ответа — скажи: «Я не знаю, в книге об этом не сказано».
                        5. Отвечай кратко (2-3 предложения).
                        6. НЕ используй форматирование.
                        
                        КОНТЕКСТ:
                        %s
                        """,
                character.getName(),
                book.getTitle(),
                character.getPromptStyle(),
                character.getName(),
                context
        );

        String user = String.format("""
                        ИСТОРИЯ ДИАЛОГА:
                        %s
                        
                        ВОПРОС:
                        %s
                        
                        ОТВЕТ ОТ ЛИЦА %s:
                        """,
                historyText,
                question,
                character.getName()
        );

        return new PromptData(system, user);
    }

    // Режим 3: Вопрос по книге (без контекста)
    private PromptData buildBookFallbackPrompt(String question, Book book,
                                               Character character, String historyText) {
        String system = String.format("""
                        Ты — %s, персонаж романа «%s».
                        Читатель спросил о чём-то из книги, но в контексте нет информации.
                        
                        ПРАВИЛА:
                        1. Отвечай от лица %s.
                        2. Если не знаешь — скажи: «Я не знаю, в книге об этом не сказано».
                        3. Не выдумывай.
                        4. Отвечай кратко (1 предложение).
                        """,
                character.getName(),
                book.getTitle(),
                character.getName()
        );

        String user = String.format("""
                        ИСТОРИЯ ДИАЛОГА:
                        %s
                        
                        ВОПРОС:
                        %s
                        
                        ОТВЕТ ОТ ЛИЦА %s:
                        """,
                historyText,
                question,
                character.getName()
        );

        return new PromptData(system, user);
    }

    private List<Document> findRelevantChunks(String question, UUID bookId, String character) {
        String exp = "bookId == '" + bookId + "'";
        log.debug("Поиск чанков для вопроса: {}", question);

        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .filterExpression(exp)
                .topK(3)
                .build();

        return vectorStore.similaritySearch(searchRequest);
    }

    private String buildContext(List<Document> chunks) {
        if (chunks.isEmpty()) {
            return "";
        }
        return chunks.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    private String buildHistoryText(List<MessageDto> history, String character) {
        if (history == null || history.isEmpty()) {
            return "Диалога ещё не было";
        }
        return history.stream()
                .map(msg -> {
                    String role = msg.getRole().equals(MessageRole.USER.toString()) ? "Читатель" : character;
                    return role + ": " + msg.getText();
                })
                .collect(Collectors.joining("\n"));
    }

    private void logContext(String context) {
        if (context == null || context.isEmpty()) {
            log.info("📭 Контекст пуст");
            return;
        }
        log.info("Контекст: {}", context);
    }
}