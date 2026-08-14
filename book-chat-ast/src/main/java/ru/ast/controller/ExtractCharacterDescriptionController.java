package ru.ast.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.ast.service.CharacterDescriptionModelService;

import java.util.UUID;

@Slf4j
@RestController
@AllArgsConstructor
public class ExtractCharacterDescriptionController {

    private final CharacterDescriptionModelService modelService;

    @GetMapping("/test")
    public String test(@RequestParam UUID bookId, @RequestParam String character) {
        return modelService.getAnswerFromModel(bookId, character);
    }
}
