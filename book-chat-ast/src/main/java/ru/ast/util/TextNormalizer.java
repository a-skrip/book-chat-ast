package ru.ast.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class TextNormalizer {

    //ссылки
    private static final Pattern FOOTNOTE_PATTERN =
            Pattern.compile("\\[\\[footnoteRef:\\d+]]");
    // 3 и более переноса -> в 1
    private static final Pattern SERVICE_MARKS =
            Pattern.compile("\\(Прим\\.\\s*[^)]*\\)|\\(тюрк\\.\\)|\\(франц\\.\\)|\\(итал\\.\\)|\\(англ\\.\\)");
    private static final Pattern EXTRA_NEWLINES = Pattern.compile("\\n{3,}");

    // Цифры-разделители страниц
    private static final Pattern PAGE_MARKS
            = Pattern.compile("\\n\\d+\\s*\\n|image\\d+\\.jpeg"); // Цифры-разделители страниц

    public String normalize(String text) {
        String result = text;
        // Создаём Matcher для подсчёта
        countFind(FOOTNOTE_PATTERN, text);
        result = FOOTNOTE_PATTERN.matcher(text).replaceAll("");

        countFind(SERVICE_MARKS, text);
        result = SERVICE_MARKS.matcher(result).replaceAll("");

        countFind(EXTRA_NEWLINES, text);
        result = EXTRA_NEWLINES.matcher(result).replaceAll("\n\n");


        countFind(PAGE_MARKS, text);
        result = PAGE_MARKS.matcher(result).replaceAll("");

        return result;
    }

    private void countFind(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);

        int count = 0;
        while (matcher.find()) {
            count++;
        }
        log.info("Найдено по паттерну: {}  >>> {}", pattern, count);
    }
}
