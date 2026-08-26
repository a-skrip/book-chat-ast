package ru.ast.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ast.dto.request.AdminRegistrationRequest;
import ru.ast.dto.request.LoginRequest;
import ru.ast.dto.response.AdminResponse;
import ru.ast.dto.response.AuthResponse;
import ru.ast.service.AdminService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Авторизация администраторов")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/register")
    @Operation(summary = "Регистрация нового администратора",
            description = "Только для существующих администраторов (нужен JWT)")
    public ResponseEntity<AdminResponse> registerAdmin(
            @RequestBody @Valid AdminRegistrationRequest request) {
        AdminResponse admin = adminService.registerAdmin(request);
        return ResponseEntity.ok(admin);

    }

    @PostMapping("/login")
    @Operation(summary = "Вход в систему",
            description = "Возвращает JWT-токен для доступа к админке")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        AuthResponse response = adminService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получить список администраторов",
            description = "Только для существующих администраторов (нужен JWT)")
    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<AdminResponse>> getAllAdmins() {
        return ResponseEntity.ok(adminService.getAllAdmins());
    }
}
