package ru.ast.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class YandexAiConfig {

    @Value("${yandex.api-key}")
    private String apiKey;

    @Value("${yandex.base-url}")
    private String baseUrl;

    @Value("${yandex.model}")
    private String model;

    @Value("${yandex.folder}")
    private String folder;

    @Bean
    public OpenAIClient yandexOpenAIClient() {
        return OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .organization(folder)
                .build();
    }

    @Bean
    public String yandexModelName() {
        return String.format("gpt://%s/%s", folder, model);
    }
}