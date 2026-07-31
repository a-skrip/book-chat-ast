package ru.ast.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ast.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findAllByChatId(UUID chatId);
}
