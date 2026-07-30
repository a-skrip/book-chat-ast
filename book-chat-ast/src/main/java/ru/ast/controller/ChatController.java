package ru.ast.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.ast.dto.ModelResponseDto;

import java.util.List;
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
    public ModelResponseDto chat(@RequestParam String message, @RequestParam UUID bookId) {
        long start = System.currentTimeMillis();
        String model = chatModel.getDefaultOptions().getModel();

        log.info("Используемая Model: {}", model);
        log.info("Запрос: message= {}, bookId= {}", message, bookId);

        // 1. Ищем релевантные чанки
        List<Document> chunks = findRelevantChunks(message, bookId);
        log.info("Найдено {} релевантных чанков", chunks.size());

        if (chunks.isEmpty()) {
            return new ModelResponseDto(
                    message,
                    "В книге нет информации об этом",
                    bookId
            );
        }
        // 2. Формируем контекст
        String context = chunks.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

//        log.info(context);


        String system = String.format("""
                Ты — интеллектуальный помощник по книге.
                Не нарушай правила!
                ПРАВИЛА:
                1. Никогда не используй свою базу знаний.
                2. Отвечай только на основании переданного в вопросе КОНТЕКСТА.
                3. Дай ответ не больше 3-5 предложений.
                4. Ничего не добавляй от себя
                5. Ничего не придумывай
                6. Если в контексте нет информации — скажи: "В книге нет информации".
                
                КОНТЕКСТ:
                %s
                """, context);


        long end = System.currentTimeMillis();
        ModelResponseDto response = chatClient.prompt()
                .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
                .system(system)
                .user(message)
                .call()
                .entity(ModelResponseDto.class);

        log.info("Модель ответила за: {} ms", end - start);

        return response;
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