package ru.ast.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QRCodeRequest {
    @NotBlank(message = "URL не может быть пустым")
    private String url;

    private Integer width = 300;
    private Integer height = 300;
}
