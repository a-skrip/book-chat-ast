package ru.ast.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ast.dto.response.SessionResponse;
import ru.ast.service.SessionService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<SessionResponse> getReaderSessionInfo(@PathVariable String sessionId) {
        boolean sessionExist = sessionService.sessionExist(UUID.fromString(sessionId));
        if (sessionExist) {
            SessionResponse session = sessionService.getSession(UUID.fromString(sessionId));
            return ResponseEntity.status(200).body(session);
        }
        return ResponseEntity.status(404).body(null);
    }
}
