package ru.ast.service;

import com.openai.client.OpenAIClient;
import com.openai.models.Reasoning;
import com.openai.models.ReasoningEffort;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputText;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
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
//@AllArgsConstructor
public class ModelChatService {

    private final OpenAIClient chatClient;
    private final String modelName;
    private final VectorStore vectorStore;

    public ModelChatService(
            @Qualifier("yandexOpenAIClient") OpenAIClient chatClient,
            @Qualifier("yandexModelName") String modelName,
            VectorStore vectorStore

    ) {
        this.chatClient = chatClient;
        this.modelName = modelName;
        this.vectorStore = vectorStore;
    }

    private static final String[] BOOK_KEYWORDS = {
            "Бэла","Бэлу","Бэле","Бэлой", "Бэлою",
            "Печорин","Печорине","Печорина","Печорину","Печориным",
            "Максим Максимыч","Максим Максимыча","Максим Максимыче","Максим Максимычу","Максим Максимычем",
            "Казбич","Казбича","Казбичу","Казбиче","Казбичем",
            "Азамат","Азамату","Азаматом","Азамате","Азамата",
            "Григорий Александрович",
            "княжна", "княжне","княжной","княжну",
            "Грушницкий","Грушницкому","Грушницком",
            "Вернер","Вернеру","Вернере","Вернером",
            "Вера", "Верой","Вере","Веру",
            "Кавказ","Кавказе","Кавказу",
            "роман", "книга", "глава", "часть", "сюжет",
            "герой", "персонаж", "история", "смерть", "любовь",
            "дуэль", "судьба", "характер", "поступок", "чувства"
    };

    public String getAnswerFromModel(String question, Book book, Character character, List<MessageDto> history) {
        String answer;

        long start = System.currentTimeMillis();
        log.info("Вопрос: '{}' | Книга: {} | Персонаж: {}",
                question, book.getTitle(), character.getName());

        // 1. Поиск чанков
        List<Document> chunks = findRelevantChunks(question, book.getId(), character.getName());
        log.info("Найдено чанков: {}", chunks.size());

        // 2. Формируем контекст и историю
        String context = buildContext(chunks);
//        log.info("Контекст: {}", context);
        String historyText = buildHistoryText(history, character.getName());
        // 3. Определяем тип вопроса
        DialogMode mode = determineDialogMode(question, chunks, history);
        log.info("🎯 Режим диалога: {}", mode);

//        if (mode.equals(DialogMode.QUESTION_AT_ANSWER)) {
//            log.info("Перезапрос контекста");
//            context = retryGetContext(book, character, history);
//        }
        // 4. Формируем промпт
        PromptData promptData = buildPrompt(mode, question, book, character, context, historyText, history);

        // 5. Отправляем запрос
//        answer = chatClient.prompt()
//                .system(promptData.system())
//                .user(promptData.user())
//                .call()
//                .content();
        ResponseCreateParams params = ResponseCreateParams.builder()
                .model(modelName)
                .temperature(0.2)
                .instructions(promptData.system)
                .input(promptData.user)
                .maxOutputTokens(1500)
                .reasoning(Reasoning.builder()
                        .effort(ReasoningEffort.NONE)
                        .build())
                .build();
//        log.info("promt_system >>> {}", promptData.system);
//        log.info("promt_user >>> {}", promptData.user);

        Response response = chatClient.responses().create(params);

//        log.info("RESPONSE >>> {}", response);

        answer = response.output().stream()
                .filter(el -> el.message().isPresent())
                .map(el -> el.message().get())
                .flatMap(rom -> rom.content().stream())
                .map(c -> c.outputText().get())
                .map(ResponseOutputText::text)
                .findFirst()
                .get();
//                .orElse("Не удалось получить ответ");


        log.info("Модель ответила за: {} ms", System.currentTimeMillis() - start);
        log.info("Ответ: {}", answer);

        return answer;
    }

    private PromptData buildPrompt(DialogMode mode, String question, Book book,
                                   Character character, String context, String historyText, List<MessageDto> history) {
        return switch (mode) {
            case FREE_DIALOG -> buildFreeDialog(question, book, character, historyText);
            case BOOK_RAG -> buildRagDialog(question, book, character, context, historyText);
            case BOOK_FALLBACK -> buildFallbackDialog(question, book, character, historyText);
//            case QUESTION_AT_ANSWER -> buildQuestionAndAnswerDialog(question, book, character, context, history);
        };
    }

//    private PromptData buildQuestionAndAnswerDialog(String question,
//                                                    Book book,
//                                                    Character character,
//                                                    String context,
//                                                    List<MessageDto> history) {
//        String system = String.format("""
//                        Ты — %s, персонаж книги «%s».
//                        Сейчас читатель задал вопрос: "%s" по твоему ответу "%s".
//
//                        ПРАВИЛА:
//                        1. Отвечай от лица %s.
//                        2. Контекст на основании которого ты дал ответ будет передан в контексте
//                        3. Ответь на вопрос читателя почему ты так ответил опираясь на контекст
//                        4. Ответь коротко - 1 предложение
//                        4. НЕ используй звёздочки, подчёркивания, скобки.
//
//                        КОНТЕКСТ:
//                        %s
//                        """,
//                character.getName(),
//                book.getTitle(),
//                question,
//                history.getLast().getText(),
//                character.getName(),
//                context
//        );
//        String user = String.format("""
//                        ВОПРОС:
//                        %s
//                        ОТВЕТ ОТ ЛИЦА %s:
//
//                        """,
//                question,
//                character.getName()
//        );
//        return new PromptData(system, user);
//    }


    // Режим 1: Свободный диалог
    private PromptData buildFreeDialog(String question,
                                       Book book,
                                       Character character,
                                       String historyText) {
        String system = String.format("""
                        Ты — %s, персонаж романа «%s».
                        Сейчас читатель задал вопрос, не связанный с книгой.
                        
                        ПРАВИЛА:
                        1. Отвечай ТОЛЬКО от лица %s.
                        2. ОБЯЗАТЕЛЬНО обращайся к читателю на «ТЫ». НИКОГДА не используй «ВЫ».
                        3. Поддерживай естественный диалог.
                        4. Будь вежлив(а) и дружелюбен(на).
                        5. Отвечай кратко (1 предложение).
                        6. НЕ используй звёздочки, подчёркивания, скобки.
                        7. НЕ копируй свои предыдущие ответы из истории диалога.
                        8. Используй историю ТОЛЬКО для понимания о чем ранее велся диалог, ОТВЕЧАЙ ПО-НОВОМУ.
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
    private PromptData buildRagDialog(String question,
                                      Book book,
                                      Character character,
                                      String context,
                                      String historyText) {

        String system = String.format("""
                        Ты — %s, персонаж романа «%s».
                        
                        ТВОЙ ХАРАКТЕР:
                        %s
                        
                        ПРАВИЛА:
                        1. Не используй свою базу знаний
                        2. Отвечай ТОЛЬКО от лица %s.
                        3. ОБЯЗАТЕЛЬНО обращайся к читателю на «ТЫ». НИКОГДА не используй «ВЫ».
                        4. Используй информацию из КОНТЕКСТА как основу для ответа.
                        5. Если в контексте есть информация по теме — используй её, даже если она косвенная.
                        6. Если вопрос о чувствах персонажа — ищи намёки, действия, эмоции в контексте.
                        7. Передай ЭМОЦИИ и ЧУВСТВА персонажа, а не просто описание действий.
                        8. Если в контексте есть намёки на чувства — раскрой их.
                        9. Отвечай кратко (1 предложение, не длинные).
                        10. НЕ используй форматирование.
                        11. НЕ копируй текст из контекста дословно — переформулируй своими словами.
                        
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

    private PromptData buildFallbackDialog(String question,
                                           Book book,
                                           Character character,
                                           String historyText) {
        log.info("История диалога: {}", historyText);
        String system = String.format("""
                        Ты — %s, персонаж романа «%s».
                        Читатель спросил о чём-то из книги, но в контексте нет информации.
                       
                        ПРАВИЛА:
                        1. Не используй свою базу знаний
                        2. Отвечай ТОЛЬКО от лица %s.
                        3. ОБЯЗАТЕЛЬНО обращайся к читателю на «ТЫ». НИКОГДА не используй «ВЫ».
                        4. Посмотри историю диалога, возможно там есть ответ
                        5. Если в истории диалога эта тема не обуждалась — скажи: «Я не знаю, в книге об этом не сказано».
                        6. НЕ выдумывай.
                        7. НЕ используй форматирование.
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
        String searchQuery = question + " " + character;

        SearchRequest searchRequest = SearchRequest.builder()
                .query(searchQuery)
                .filterExpression(exp)
                .topK(3)
                .build();
        log.info("Вызов similaritySearch()");
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

//    private boolean hasQuestionAtAnswer(List<MessageDto> history, String question) {
//        String regex = "[^А-Яа-яЁё]+";
//        Pattern pattern = Pattern.compile(regex);
//        String[] splitQuestion = question.split(pattern.toString());
//        MessageDto messageDto = history.getLast();
//        String answerFromModel = messageDto.getText();
//        String[] splitAnswer = answerFromModel.split(regex);
//
//        for (String wordQuestion : splitQuestion) {
//            String questionLowerCase = wordQuestion.toLowerCase();
//            for (String wordAnswer : splitAnswer) {
//                String answerLowerCase = wordAnswer.toLowerCase();
//                if (questionLowerCase.equals(answerLowerCase)) {
//                    log.info("{} >>> {}", wordQuestion, wordAnswer);
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    private enum DialogMode {
        FREE_DIALOG,
        BOOK_RAG,
        BOOK_FALLBACK
//        QUESTION_AT_ANSWER
    }

    private DialogMode determineDialogMode(String question, List<Document> chunks, List<MessageDto> history) {
        boolean isBookQuestion = isQuestionAboutBook(question);
        boolean hasContext = !chunks.isEmpty();
//        boolean atAnswer = hasQuestionAtAnswer(history, question);

//        if (atAnswer) {
//            return DialogMode.QUESTION_AT_ANSWER;
//        }
        if (isQuestionAboutBook(question) && !chunks.isEmpty()) {
            return DialogMode.BOOK_RAG;
        }
        if (!isBookQuestion) {
            return DialogMode.FREE_DIALOG;
        }
        return DialogMode.BOOK_FALLBACK;
    }

    private record PromptData(String system, String user) {
    }

    private boolean isQuestionAboutBook(String question) {
        String lower = question.toLowerCase();
        for (String keyword : BOOK_KEYWORDS) {
            if (lower.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

//    private boolean isNonBookQuestion(String question) {
//        String lower = question.toLowerCase();
//        String[] nonBookPatterns = {
//                "как дела", "как жизнь", "как настроение", "как ты",
//                "привет", "здравствуй", "добрый день", "доброе утро",
//                "пока", "до свидания", "спокойной ночи",
//                "как погода", "что нового", "как сам", "как твои"
//        };
//        for (String pattern : nonBookPatterns) {
//            if (lower.contains(pattern)) {
//                return true;
//            }
//        }
//        return false;
//    }

    private String retryGetContext(Book book, Character character, List<MessageDto> history) {
        String questionReader = history.get(history.size() - 2).getText();
        String answerModel = history.getLast().getText();
        log.info("Вопрос: {} на ответ: {}", questionReader, answerModel);

        // 1. Поиск чанков
        List<Document> chunks = findRelevantChunks(questionReader, book.getId(), character.getName());
        return buildContext(chunks);
    }
}

