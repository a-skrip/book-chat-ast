package ru.ast.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ast.entity.Character;

import java.util.List;
import java.util.UUID;

public interface CharacterRepository extends JpaRepository<Character, UUID> {

    boolean existsCharactersByBookId(UUID bookId);

    List<Character> findAllByBookId(UUID bookId);
}
