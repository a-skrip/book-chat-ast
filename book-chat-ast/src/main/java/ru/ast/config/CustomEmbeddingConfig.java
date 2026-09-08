package ru.ast.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class CustomEmbeddingConfig {

    @Value("${embedding.base-url}")
    private String baseUrl;

    @Value("${embedding.api-key}")
    private String apiKey;

    @Bean
    @Primary
    public EmbeddingModel customEmbeddingModel() {
        OpenAiApi api = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();
        return new OpenAiEmbeddingModel(api);
    }

    @Bean
    public VectorStore vectorStore(
            EmbeddingModel model,
            JdbcTemplate template
    ) {
        return PgVectorStore.builder(template, model)
                .build();
    }

}
