package ru.ast.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ast.dto.AdminDto;
import ru.ast.dto.RegisterAdminRequest;
import ru.ast.service.AdminService;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/admins")
@AllArgsConstructor
@Tag(name = "Admins")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/register")
    public ResponseEntity<AdminDto> createAdmin(@RequestBody @Valid RegisterAdminRequest request) {
        AdminDto dto = adminService.createAdmin(request);
        return ResponseEntity.created(URI.create("/admins/register" + dto.getId()))
                .body(dto);

    }
}
