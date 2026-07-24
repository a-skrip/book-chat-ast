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
import ru.ast.service.BookService;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final OpenAiChatModel chatModel;


    @GetMapping("/chat")
    public Map<String, String> chat(@RequestParam String message, @RequestParam UUID bookId) {
        long start = System.currentTimeMillis();
        String model = chatModel.getDefaultOptions().getModel();

        log.info("Используемая Model: {}", model);
        log.info("Запрос: message= {}, bookId= {}", message, bookId);

        // 1. Ищем релевантные чанки
        List<Document> chunks = findRelevantChunks(message, bookId);
        log.info("Найдено {} релевантных чанков", chunks.size());

        if (chunks.isEmpty()) {
            return Map.of(
                    "question", message,
                    "answer", "Извините, в книге не найдено информации по вашему вопросу.",
                    "chunksUsed", "0",
                    "bookId", bookId.toString()
            );
        }

        // 2. Формируем контекст
        String context = chunks.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        log.info(context);


        String system = """
                Ты — интеллектуальный помощник по книге.
                Не нарушай правила!
                ПРАВИЛА:
                1. Отвечай ТОЛЬКО НА ОСНОВЕ КОНТЕКСТА.
                2. Дай ответ не больше 3-5 предложений.
                3. Не придумывай в ответе ничего и не добавляй от себя.
                4. Если в контексте нет информации — скажи: "В книге нет информации".
                """;

        String user = String.format("""
                КОНТЕКСТ:
                %s
                
                ВОПРОС:
                %s
                
                ОТВЕТ:
                """, context, message);

        String answer = chatClient.prompt()
                .system(system)
                .user(user)
                .call()
                .content();

        long end = System.currentTimeMillis();
        log.info("Модель ответила за: {} ms", end - start);

        return Map.of(
                "question", message,
                "answer", answer,
                "chunksUsed", String.valueOf(chunks.size()),
                "bookId", bookId.toString()
        );
    }

    private List<Document> findRelevantChunks(String question, UUID bookId) {
        long start = System.currentTimeMillis();
        String exp = "bookId == '" + bookId + "'";

        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .filterExpression(exp)
                .topK(3)
                .build();

        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        long end = System.currentTimeMillis();
        log.info("Время поиска в VectorStore {}", end - start);

        return documents;
    }
}