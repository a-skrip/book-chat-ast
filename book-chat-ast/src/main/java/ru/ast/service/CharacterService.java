package ru.ast.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import ru.ast.dto.CharacterRequestDto;
import ru.ast.dto.CharacterResponseDto;
import ru.ast.dto.CharactersResponseDto;
import ru.ast.entity.Book;
import ru.ast.entity.Character;
import ru.ast.exceptions.BookNotFoundException;
import ru.ast.exceptions.CharacterNotFoundException;
import ru.ast.exceptions.SendNullInRequestException;
import ru.ast.mapper.CharacterMapper;
import ru.ast.repository.BookRepository;
import ru.ast.repository.CharacterRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class CharacterService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final CharacterRepository characterRepository;
    private final ObjectMapper objectMapper;
    private final BookRepository bookRepository;


    public CharactersResponseDto extractCharacters(UUID bookId) {
        boolean existCharacters = characterRepository.existsCharactersByBookId(bookId);
        if (bookId == null) {
            throw new IllegalArgumentException("bookId is null");
        }
        CharactersResponseDto response = new CharactersResponseDto();
        response.setBookId(bookId.toString());

        if (existCharacters) {
            log.info("Герои уже сохранены, возврат существующих");
            return getAllCharactersForBook(bookId);
        }
        String extractCharacter = extractCharacter(bookId);
        List<CharacterResponseDto> characterDtos = saveCharacterFromJson(bookId, extractCharacter);
        response.setCharacters(characterDtos);
        return response;
    }

    public CharactersResponseDto getAllCharactersForBook(UUID bookId) {
        log.info("Получение персонажей по книге id: {}", bookId);
        if (bookId == null) {
            throw new IllegalArgumentException("bookId is null");
        }
        CharactersResponseDto responseDto = new CharactersResponseDto();
        responseDto.setBookId(bookId.toString());

        List<Character> characters = characterRepository.findAllByBookId(bookId);
        List<CharacterResponseDto> dtos = characters.stream()
                .map(CharacterMapper::toDto)
                .toList();
        responseDto.setCharacters(dtos);

        return responseDto;
    }

    public CharacterResponseDto getCharacter(UUID characterId) {
        log.info("Получение персонажа по id: {}", characterId);
        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new CharacterNotFoundException(characterId));
        return CharacterMapper.toDto(character);
    }

    public CharacterResponseDto updateCharacter(UUID characterId, CharacterRequestDto request) {

        Character entity = characterRepository.findById(characterId).orElseThrow(
                () -> new CharacterNotFoundException(characterId));
        if (request.getEnabled() == null) {
            throw new SendNullInRequestException();
        }
        log.info("Обновление персонажа: {}, id: {}", entity.getName(), entity.getId());

        if (entity.isEnabled() != request.getEnabled()) {
            entity.setEnabled(request.getEnabled());
        }
        if (request.getAvatarPath() != null) {
            entity.setAvatarPath(request.getAvatarPath());
        }

        Character saved = characterRepository.save(entity);

        return CharacterMapper.toDto(saved);
    }

    private List<CharacterResponseDto> saveCharacterFromJson(UUID bookId, String jsonCharacters) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(BookNotFoundException::new);

        List<String> names = parseJsonCharacters(jsonCharacters);
        List<Character> characters = new ArrayList<>();

        for (String name : names) {
            Character character = new Character();
            character.setName(name);
            character.setEnabled(true);
            character.setBook(book);
            characters.add(character);
        }
        log.info("Сохранение персонажей в БД для bookId: {}", bookId);
        List<Character> savedCharacters = characterRepository.saveAll(characters);

        return CharacterMapper.toDtoList(savedCharacters);
    }

    private List<String> parseJsonCharacters(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);

            if (root.has("characters") && root.get("characters").isArray()) {
                log.info("Парсинг ответа модели: {}", root.get("characters"));

                List<String> names = new ArrayList<>();

                for (JsonNode character : root.get("characters")) {
                    names.add(character.asText());
                }
                return names;
            }
            log.warn("JSON не содержит поля 'characters' или оно не является массивом");
            return List.of();

        } catch (JsonProcessingException e) {
            log.error("Ошибка парсинга: " + e.getMessage());
            log.error("Ответ {}", jsonResponse);
            return List.of();
        }
    }

    private String extractCharacter(UUID bookId) {
        List<Document> relevantChunks = findRelevantChunks(bookId);

        String context = relevantChunks.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n-----\n\n"));
        //TODO проверить использует ли модель свою базу знаний
        String system = """
                Извлеки только имена персонажей художественного произведения из фрагментов ниже.
                                    Нужны только люди и действующие герои текста.
                                    Не включай автора, редакторов, исторических деятелей, мифологические или литературные сравнения,
                                    географические объекты, названия, абстрактные группы людей и случайные слова с заглавной буквы.
                                    Не включай одиночные титулы без имени, например нельзя возвращать просто "княжна" или "доктор".
                                    Если персонаж назван с титулом, возвращай полную форму, например "княжна Мери".
                                    Верни ТОЛЬКО JSON-строку.
                                    НЕ используй маркеры кода (```json, ```).
                                    Формат: {"characters": ["Имя1", "Имя2", "Имя3"]}
                                    Хорошие примеры: ["Печорин", "Максим Максимыч", "Бэла", "Азамат", "Казбич", "Вернер", "княжна Мери"].
                                    Плохие примеры: ["женщины", "Кольцо", "Нерон", "Тасс", "Римские мужи", "княжна"].
                """;

        String user = String.format("""
                Найди всех персонажей в этих фрагментах и верни JSON-массив строк.
                Контекст:
                %s
                """, context);
        String content = chatClient.prompt()
                .system(system)
                .user(user)
                .call()
                .content();
        log.info("Ответ на запрос извлечения персонажей: \n {}", content);
        return content;
    }

    private List<Document> findRelevantChunks(UUID bookId) {
        log.info("Извлечение персонажей из книги id: {}", bookId);
        String exp = "bookId == '" + bookId + "'";

        String question = "главные и второстепенные герои, персонажи, имена";

        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .filterExpression(exp)
                .topK(10)
                .build();

        return vectorStore.similaritySearch(searchRequest);
    }
}
