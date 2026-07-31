package ru.ast.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ast.dto.ReaderDto;
import ru.ast.entity.Reader;
import ru.ast.exceptions.NameNoSendException;
import ru.ast.mapper.ReaderMapper;
import ru.ast.repository.ReaderRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReaderService {

    private final ReaderRepository readerRepository;

    public ReaderDto saveReader(String name) {
        if (name == null || name.isEmpty()) {
            throw new NameNoSendException();
        }
        Reader entity = new Reader();
        entity.setName(name);
        Reader saved = readerRepository.save(entity);
        log.info("Создан name = {} c id = {}", name, saved.getId());
        return ReaderMapper.toDto(saved);
    }
}
