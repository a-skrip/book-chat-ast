package ru.ast.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ast.dto.request.QRCodeRequest;
import ru.ast.dto.response.QRCodeResponse;
import ru.ast.service.QRCodeService;

@Slf4j
@RestController
@RequestMapping("/qr" )
@RequiredArgsConstructor
@Tag(name = "QR-коды", description = "Генерация QR-кодов" )
public class QRController {

    private final QRCodeService qrCodeService;

    /**
     * Сгенерировать QR-код и вернуть как изображение PNG
     */
    @Operation(summary = "Сгенерировать QR-код", description = "Возвращает QR-код как изображение PNG" )
    @PostMapping(value = "/generate", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQRCode(@Valid @RequestBody QRCodeRequest request) {
        log.info("Генерация QR-кода для URL: {}", request.getUrl());

        String validatedUrl = qrCodeService.validateUrl(request.getUrl());
        log.info("URL после валидации: {}", validatedUrl);

        byte[] qrImage = qrCodeService.generateQRCodeBytes(
                validatedUrl,
                request.getWidth(),
                request.getHeight()
        );

        // Генерируем имя файла
        String filename = "qr_" + System.currentTimeMillis() + ".png";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + filename + "\"" )
                .contentType(MediaType.IMAGE_PNG)
                .body(qrImage);
    }

    /**
     * Сгенерировать QR-код как Base64 (для отображения в HTML)
     */
    @Operation(summary = "Сгенерировать QR-код как Base64",
            description = "Возвращает QR-код как data:image/png;base64" )
    @PostMapping("/generate/base64" )
    public ResponseEntity<QRCodeResponse> generateQRCodeBase64(@Valid @RequestBody QRCodeRequest request) {
        log.info("Генерация QR-кода (Base64) для URL: {}", request.getUrl());

        String validatedUrl = qrCodeService.validateUrl(request.getUrl());
        String base64 = qrCodeService.generateQRCodeBase64(
                validatedUrl,
                request.getWidth(),
                request.getHeight()
        );

        QRCodeResponse response = new QRCodeResponse();
        response.setQrCode(base64);
        response.setUrl(validatedUrl);
        response.setWidth(request.getWidth());
        response.setHeight(request.getHeight());

        return ResponseEntity.ok(response);
    }

    /**
     * Сгенерировать QR-код для чата (без тела запроса, используем query param)
     */
    @Operation(summary = "Сгенерировать QR-код для чата",
            description = "Возвращает QR-код со ссылкой на чат" )
    @GetMapping(value = "/chat", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateChatQR(
            @RequestParam(defaultValue = "http://109.172.88.224:8080/chats?sessionId=e7c1d3f1-902b-49a9-952d-6d5d4654aa53" ) String url,
            @RequestParam(defaultValue = "300" ) int width,
            @RequestParam(defaultValue = "300" ) int height) {

        log.info("Генерация QR-кода для чата: {}", url);

        String validatedUrl = qrCodeService.validateUrl(url);
        byte[] qrImage = qrCodeService.generateQRCodeBytes(validatedUrl, width, height);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"qr-chat.png\"" )
                .contentType(MediaType.IMAGE_PNG)
                .body(qrImage);
    }
}