package ru.ast.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CharacterChatController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final OpenAiChatModel chatModel;

    @GetMapping("/characters")
    public String test(@RequestParam String message,
                       @RequestParam UUID bookId,
                       @RequestParam String character) {
        long startTime = System.currentTimeMillis();

        List<Document> chunks = findRelevantChunks(message, bookId);
        log.info("Найдено {} релевантных чанков", chunks.size());

        if (chunks.isEmpty()) {
            return "Информации не нашлось";
        }
        // 2. Формируем контекст

        String context = chunks.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        // 3. System-промпт
        String system = String.format("""
                Ты — герой книги "Герой нашего времени" — %s.
                
                ЖЁСТКОЕ ПРАВИЛО:
                1. Отвечай ТОЛЬКО от лица %s.
                2. Используй ТОЛЬКО слова %s из контекста.
                3. НЕ ВЫДУМЫВАЙ диалогов и событий.
                4. НЕ используй фразы других персонажей.
                5. Если в контексте нет информации — скажи: "Я не помню этого".
                6. Отвечай кратко, как персонаж.
                7. НЕ говори о себе в третьем лице.
                """, character, character, character);

        // 4. User-промпт с контекстом
        String user = String.format("""
                КОНТЕКСТ
                %s
                
                ВОПРОС: %s
                
                ОТВЕТЬ КОРОТКО, КАК %s (только из контекста):
                """, context, message, character);

        // 5. Отправляем запрос
        String response = chatClient.prompt()
                .system(system)
                .user(user)
                .call()
                .content();

        long endTime = System.currentTimeMillis();
        log.info("Модель ответила за: {} ms", endTime - startTime);

        return response;
    }

    private List<Document> findRelevantChunks(String question, UUID bookId) {
        String exp = "bookId == '" + bookId + "'";

        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .filterExpression(exp)
                .topK(3)
                .build();

        return vectorStore.similaritySearch(searchRequest);
    }
}
