package ru.ast.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ast.dto.request.RequestQRGenerate;
import ru.ast.service.QRCodeService;

@Slf4j
@RestController
@RequestMapping("/qr")
@RequiredArgsConstructor
@Tag(name = "QR-коды", description = "Генерация QR-кодов")
public class QRController {

    private final QRCodeService qrCodeService;

//
    /**
     * Сгенерировать QR-код для чата (без тела запроса, используем query param)
     */
    @Operation(summary = "Сгенерировать QR-код для чата",
            description = "Возвращает QR-код со ссылкой на чат")
    @PostMapping(value = "/chat", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateChatQR(@RequestBody RequestQRGenerate request) {


        byte[] qrImage = qrCodeService.generateQRCodeBytes(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"qr-chat.png\"")
                .contentType(MediaType.IMAGE_PNG)
                .body(qrImage);
    }
}