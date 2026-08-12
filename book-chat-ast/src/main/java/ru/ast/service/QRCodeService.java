package ru.ast.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ast.dto.request.RequestQRGenerate;
import ru.ast.entity.Book;
import ru.ast.exceptions.BookNotFoundException;
import ru.ast.repository.BookRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class QRCodeService {

    private final BookRepository bookRepository;

    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 300;

    /**
     * Генерирует QR-код как массив байтов (PNG)
     */
    public byte[] generateQRCodeBytes(RequestQRGenerate request) {
        UUID bookId = UUID.fromString(request.getBookId());

        StringBuilder builder = new StringBuilder();
        builder.append("http://")
                .append(request.getHost())
                .append(":")
                .append(request.getPort())
                .append(request.getUrl())
                .append(request.getBookId());
        log.info("Сформирован URL {}", builder);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        String qrCodeBase64 = generateQRCodeBase64(builder.toString());
        book.setQrCode(qrCodeBase64);
        bookRepository.save(book);
        log.info("QR сохранен для книги: {}", book.getTitle());


        return generateQRCodeBytes(builder.toString(), DEFAULT_WIDTH, DEFAULT_HEIGHT);
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
     * Генерирует QR-код как Base64-строку с кастомным размером
     */
    public String generateQRCodeBase64(String content) {
        byte[] imageBytes = generateQRCodeBytes(content, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        return "data:image/png;base64," + base64;
    }
}