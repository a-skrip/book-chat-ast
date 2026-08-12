package ru.ast.dto.request;

import lombok.Data;

@Data
public class RequestQRGenerate {
    private String host;
    private String port;
    private String url;
    private String bookId;

}
