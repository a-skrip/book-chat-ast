package ru.ast.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatRequestNewDto {
    @NotNull
    private String bookId;
    @NotNull
    private String characterId;
    @NotNull
    private String message;

    private String conversationId;
    private String sessionId;
    private String relationshipNote;
    private final String model = "Mistral";
    private final int maxCanonChunks = 3;
}
