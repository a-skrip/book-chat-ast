package ru.ast.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LangChainChunkingService {

    private final DocumentSplitter documentSplitter;

    public List<org.springframework.ai.document.Document> splitText(String fullText, UUID bookId) {
        log.info("Разбиение текста через LangChain4j, размер: {} символов", fullText.length());

        // 1. Создаём документ LangChain4j
        Map<String, Object> meta = new HashMap<>();
        meta.put("bookId", bookId.toString());

        Metadata metadata = new Metadata(meta);

        Document langChainDoc = Document.from(fullText, metadata);

        // 2. Разбиваем на чанки (с overlap)
        List<TextSegment> segments = documentSplitter.split(langChainDoc);
        log.info("Создано {} сегментов с перекрытием", segments.size());

        // 3. Конвертируем в Spring AI Document
        List<org.springframework.ai.document.Document> chunks = new ArrayList<>();
        for (TextSegment segment : segments) {
            // ✅ Правильное преобразование Metadata в Map

            // Копируем все поля из Metadata
            Map<String, Object> chunkMetadata = new HashMap<>(segment.metadata().toMap());

            org.springframework.ai.document.Document chunk =
                    new org.springframework.ai.document.Document(segment.text(), chunkMetadata);
            chunks.add(chunk);
        }

        log.info("Текст разбит на {} чанков", chunks.size());
        return chunks;
    }
}