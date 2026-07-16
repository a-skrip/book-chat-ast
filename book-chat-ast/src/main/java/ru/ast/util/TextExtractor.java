package ru.ast.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;

import java.io.File;
import java.io.IOException;

@Slf4j
public class TextExtractor {

    public static String extractTextFromFile(String filePath)
            throws IOException, TikaException {

        Tika tika = new Tika();
        File file = new File(filePath);

        log.info("Определен файл типа: {}", tika.detect(file));
        log.info("Обработка файла: {}", file.getName());
        log.info("Размер: {} байт", file.length());

        tika.setMaxStringLength(-1);
        String fullText = tika.parseToString(file);

        log.info("Извлечено {} символов", fullText.length());
        return fullText;
    }
}
