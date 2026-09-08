package ru.ast.service;

import com.openai.client.OpenAIClient;
import com.openai.models.Reasoning;
import com.openai.models.ReasoningEffort;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputText;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
//import ru.ast.util.MistralHealthIndicator;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
//@AllArgsConstructor
public class CharacterDescriptionModelService {

    private final OpenAIClient chatClient;
    private final String modelName;
    private final VectorStore vectorStore;
//    private final MistralHealthIndicator mistralHealthIndicator;

    public CharacterDescriptionModelService(
            @Qualifier("yandexOpenAIClient")OpenAIClient chatClient,
            @Qualifier("yandexModelName")String modelName,
            VectorStore vectorStore
//            MistralHealthIndicator mistralHealthIndicator
    ) {
        this.chatClient = chatClient;
        this.modelName = modelName;
        this.vectorStore = vectorStore;
//        this.mistralHealthIndicator = mistralHealthIndicator;
    }

    public String getAnswerFromModel(UUID bookId, String character) {
        long start = System.currentTimeMillis();
        log.info("Книга: {}, персонаж: {}", bookId, character);

//        mistralHealthIndicator.health();

        List<Document> chunks = findRelevantChunks(character, bookId, character);

        if (chunks.isEmpty()) {
            return "Чанков нет, в книге нет информации об этом";
        }

        String context = chunks.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        String system = String.format("""
                Не нарушай правила!
                ЖЁСТКИЕ ПРАВИЛА:
                1. Никогда не используй свою базу знаний.
                2. Отвечай только на основании переданного в вопросе КОНТЕКСТА.
                3. Ответь коротко одно предложение
                
                Пример плохого ответа : Печорин — противоречивый, скептичный и эгоцентричный человек с холодным умом и страстной натурой.
                Пример хорошего ответа: противоречивый, скептичный и эгоцентричный человек с холодным умом и страстной натурой.
                
                КОНТЕКСТ:
                %s
                """, context);

        String user = String.format("""
                Задача:
                Опиши коротко темперамент персонажа "Герой нашего времени" — %s.
                
                """, character);

        log.info("Отправлен запрос к моделе - chunks.size: {}", chunks.size());
//        String answer = chatClient.prompt()
//                .system(system)
//                .user(user)
//                .call()
//                .content();
        PromptData promptData = new PromptData(system, user);

        ResponseCreateParams params = ResponseCreateParams.builder()
                .model(modelName)
                .temperature(0.3)
                .instructions(promptData.system)
                .input(promptData.user)
                .maxOutputTokens(1500)
                .reasoning(Reasoning.builder()
                        .effort(ReasoningEffort.NONE)
                        .build())
                .build();
        Response response = chatClient.responses().create(params);

        String answer = response.output().stream()
                .filter(el -> el.message().isPresent())
                .map(el -> el.message().get())
                .flatMap(rom -> rom.content().stream())
                .map(c -> c.outputText().get())
                .map(ResponseOutputText::text)
                .findFirst()
                .get();

        long endTime = System.currentTimeMillis();
        log.info("Модель ответила за: {} ms", endTime - start);

        return answer;
    }

    private List<Document> findRelevantChunks(String question, UUID bookId, String character) {
        String exp = "bookId == '" + bookId + "'";
        String searchQuery = question + " " + character;

        SearchRequest searchRequest = SearchRequest.builder()
                .query(searchQuery)
                .filterExpression(exp)
                .topK(3)
                .build();
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        log.info("Найдено релевантных чанков: {} ", documents.size());
        return documents;
    }
    private record PromptData(String system, String user) {
    }
}
