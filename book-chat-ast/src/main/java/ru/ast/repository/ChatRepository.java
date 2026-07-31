package ru.ast.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ast.entity.Chat;

import java.util.Optional;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {

    Optional<Chat> findChatBySessionIdAndCharacterId(UUID id, UUID characterId);

}
