package ru.ast.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Slf4j
@Service
public class QRCodeService {

    private static final int DEFAULT_WIDTH = 500;
    private static final int DEFAULT_HEIGHT = 500;

    /**
     * Генерирует QR-код как массив байтов (PNG)
     */
    public byte[] generateQRCodeBytes(String content) {
        return generateQRCodeBytes(content, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Генерирует QR-код как массив байтов (PNG) с кастомным размером
     */
    public byte[] generateQRCodeBytes(String content, int width, int height) {
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("Содержимое QR-кода не может быть пустым");
        }

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    width,
                    height
            );

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            return baos.toByteArray();

        } catch (WriterException | IOException e) {
            log.error("Ошибка генерации QR-кода: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось сгенерировать QR-код", e);
        }
    }

    /**
     * Генерирует QR-код как Base64-строку (data:image/png;base64,...)
     */
    public String generateQRCodeBase64(String content) {
        return generateQRCodeBase64(content, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Генерирует QR-код как Base64-строку с кастомным размером
     */
    public String generateQRCodeBase64(String content, int width, int height) {
        byte[] imageBytes = generateQRCodeBytes(content, width, height);
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        return "data:image/png;base64," + base64;
    }

    /**
     * Валидация URL перед генерацией QR
     */
    public String validateUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        return url;
    }
}