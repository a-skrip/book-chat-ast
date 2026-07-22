package ru.ast.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    private final ChatModel chatModel;


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

        // 3. Промпт с контекстом
        String prompt = """
                Ты — интеллектуальный помощник по книге.
                Отвечай ТОЛЬКО на основе информации из контекста и ТОЛЬКО на русском языке.
                Если в контексте нет ответа, скажи: "В книге нет информации об этом".
                Не придумывай и не добавляй информацию из своих знаний.
                КОНТЕКСТ ИЗ КНИГИ:
                %s
                
                ВОПРОС: %s
                
                ОТВЕТ:""".formatted(context, message);

        String answer = chatClient.prompt()
                .user(prompt)
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

    /**
     * Поиск релевантных чанков по книге
     */
    private List<Document> findRelevantChunks(String question, UUID bookId) {
//        FilterExpressionBuilder builder = new FilterExpressionBuilder();
//        var filter = builder.eq("bookId", bookId.toString());
        long start = System.currentTimeMillis();
        String exp = "bookId == '" + bookId + "'";

        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .filterExpression(exp)
                .topK(5)
                .build();

        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        long end = System.currentTimeMillis();
        log.info("Время поиска в VectorStore {}", end - start);

        return documents;
    }
}