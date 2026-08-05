package ru.ast.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.ast.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findAllByChatIdOrderByCreatedAtAsc(UUID chatId);

    List<Message> findTop7ByChatIdOrderByCreatedAtDesc(UUID chatId);

    Message findTopByChatIdOrderByCreatedAtDesc(UUID chatId);

    @Query(value = """
            SELECT * FROM messages
            WHERE chat_id = :chatId AND message_role = 'USER'
            ORDER BY created_at ASC LIMIT 1
            """
            , nativeQuery = true)
    Message findFirstMessageFromReader(UUID chatId);
}
