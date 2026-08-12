package ru.ast.dto.response;

import lombok.Data;

@Data
public class QRCodeResponse {
    private String qrCode;      // data:image/png;base64,...
    private String url;         // исходный URL
    private Integer width;
    private Integer height;
}