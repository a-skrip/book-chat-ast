package ru.ast.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ast.entity.Chat;
import ru.ast.entity.Message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {

    Optional<Chat> findChatBySessionIdAndCharacterId(UUID chatId, UUID characterId);

    List<Chat> findChatsBySessionId(UUID sessionId);




}
