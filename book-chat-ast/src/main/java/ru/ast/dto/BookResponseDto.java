package ru.ast.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookResponseDto {
    private UUID id;
    private String title;
    private String fullText;
    private String status;
    private String uploadPath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
