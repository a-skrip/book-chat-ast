package ru.ast.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ast.entity.ReaderSession;

import java.util.Optional;
import java.util.UUID;

public interface ReaderSessionRepository extends JpaRepository<ReaderSession, UUID> {


    Optional<ReaderSession> findReaderSessionByReaderId(UUID readerId);
}
