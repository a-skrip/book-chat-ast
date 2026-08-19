package ru.ast.util;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mistralai.MistralAiEmbeddingModel;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class MistralHealthIndicator implements HealthIndicator {

    private final MistralAiEmbeddingModel embeddingModel;

    @Override
    public Health health() {
        try {
            String ping = "ping";
            embeddingModel.embed(ping);

            log.info("✅ Mistral AI доступен");

            return Health.up()
                    .withDetail("service", "Mistral AI")
                    .withDetail("status", "available")
                    .build();


        } catch (Exception e) {
            log.warn("❌ Mistral AI недоступен: {}", e.getMessage());
            return Health.down()
                    .withDetail("service", "Mistral AI")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

    public boolean isAvailable() {
        return health().getStatus().getCode().equals("UP");
    }
}