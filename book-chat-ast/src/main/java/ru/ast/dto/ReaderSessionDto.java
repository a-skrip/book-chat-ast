package ru.ast.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReaderSessionDto {

    private String id;
    private String bookId;
    private String readerId;
    private List<ChatDto> chats;
}
