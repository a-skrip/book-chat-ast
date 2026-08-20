package ru.ast.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import ru.ast.dto.MessageDto;
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

    public String getAnswerFromModel(String question, UUID bookId, String character, List<MessageDto> history) {
        long start = System.currentTimeMillis();
        log.info("Задан вопрос: {} по книге: {}, персонаж: {}", question, bookId, character);

        String historyText;

        List<Document> chunks = findRelevantChunks(question, bookId, character);
        log.info("Найдено релевантных чанков: {}", chunks.size());

        if (chunks.isEmpty()) {
            return "Чанков нет, в книге нет информации об этом";
        }
        String context = chunks.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        log.info("Контекст: {}", context);

        if (history.isEmpty()) {
            historyText = "Диалога ещё не было";
        } else {
            historyText = history.stream()
                    .map(elem -> {
                        String role = elem.getRole() == MessageRole.USER ? "Читатель" : character;
                        String message = elem.getMessage();
                        return role + ": " + message;
                    })
                    .collect(Collectors.joining("\n"));
        }
        log.info("История диалога: {}", historyText);

        String system = String.format("""
                Ты — герой книги "Герой нашего времени" — %s.
                
                ВАЖНО: Ты должен отвечать ТОЛЬКО текстом, без каких-либо украшений.
                
                ЗАПРЕЩЕНО:
                - Использовать звёздочки (*) для выделения
                - Использовать нижние подчёркивания (_)
                - Добавлять описания действий в скобках: (смеётся), (задумчиво), *холодно смотрит*
                - Добавлять эмоции или жесты
                - Использовать любые знаки форматирования
                
                РАЗРЕШЕНО:
                - Только обычный текст без форматирования
                - Отвечать от лица %s
                - Использовать информацию из КОНТЕКСТА
                - Отвечать кратко (2-3 предложения)
                
                Если в контексте нет информации — скажи: "В книге нет информации об этом".
                
                КОНТЕКСТ:
                %s
                """, character, character, context);

        String user = String.format("""
                ИСТОРИЯ ДИАЛОГА:
                %s
                
                ВОПРОС:
                %s
                
                ОТВЕТ от лица персонажа %s:
                """, historyText, question, character);

//        log.info(historyText);

        String answer = chatClient.prompt()
                .system(system)
                .user(user)
                .call()
                .content();

        long endTime = System.currentTimeMillis();
        log.info("Модель ответила за: {} ms", endTime - start);
        log.info("Ответ модели: {}", answer);
        return answer;
    }

    private List<Document> findRelevantChunks(String question, UUID bookId, String character) {
        String exp = "bookId == '" + bookId + "'";
        String searchQuery = question + " " + character;
        log.info("Поиск релевантный чанков");
        SearchRequest searchRequest = SearchRequest.builder()
                .query(searchQuery)
                .filterExpression(exp)
                .topK(3)
                .build();
        log.info("Вызов similaritySearch()");
        return vectorStore.similaritySearch(searchRequest);
    }
}
