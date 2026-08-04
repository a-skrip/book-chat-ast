package ru.ast.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class ReaderSessionDto {
    private UUID sessionId;
    private List<MessageDto> messages;
}
