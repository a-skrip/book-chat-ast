package ru.ast.service;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


@Slf4j
@Service
@Getter
@Setter
public class FileUploadService {

    private static final String UPLOAD_TO = "src/main/resources/data";


    public String upload(String path) {
        String pathToLocalFile = "";

        try {
            Path pathToFile = Path.of(path);
            if (!Files.exists(pathToFile)) {
                throw new IllegalArgumentException("Файл не найден " + path);
            }
            Path pathToUpload = Paths.get(UPLOAD_TO);
            if (!Files.exists(pathToUpload)) {
                Files.createDirectories(pathToUpload);
                log.info("Создана директория {}", pathToUpload.toAbsolutePath());
            }
            String fileName = pathToFile.getFileName().toString();
            Path pathFile = pathToUpload.resolve(fileName);
            pathToLocalFile = pathFile.toAbsolutePath().toString();

            Files.copy(pathToFile, pathFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("Файл cкопирован {} -> {}", pathToFile, pathFile.toAbsolutePath());


        } catch (IOException e) {
            log.error("Ошибка копирования файла {} -> {} ", path, e.getMessage());
        }
        return pathToLocalFile;
    }
}
