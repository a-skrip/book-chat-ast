package ru.ast.dto;

import lombok.Getter;
import lombok.Setter;
import ru.ast.entity.ReaderSession;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ReaderDto {
    private UUID id;
    private String name;
    private List<ReaderSession> sessions;
}
