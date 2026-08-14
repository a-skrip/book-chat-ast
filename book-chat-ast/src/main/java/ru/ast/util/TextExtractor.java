package ru.ast.util;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Slf4j
@Component
@AllArgsConstructor
public class TextExtractor {

    private final TextNormalizer normalizer;

    public String extractTextFromFile(String filePath)
            throws IOException, TikaException {

        Tika tika = new Tika();
        File file = new File(filePath);

        log.info("Определен файл типа: {}", tika.detect(file));
        log.info("Обработка файла: {}", file.getName());
        log.info("Размер: {} байт", file.length());

        tika.setMaxStringLength(-1);
        String fullText = tika.parseToString(file);

        String normalized = normalizer.normalize(fullText).trim();
        log.info("Текст книги нормализован");

        return normalized;
    }
}
