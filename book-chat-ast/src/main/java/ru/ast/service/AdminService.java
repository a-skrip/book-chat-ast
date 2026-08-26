package ru.ast.service;


import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.ast.dto.request.AdminRegistrationRequest;
import ru.ast.dto.request.LoginRequest;
import ru.ast.dto.response.AdminResponse;
import ru.ast.dto.response.AuthResponse;
import ru.ast.entity.Admin;
import ru.ast.enums.Role;
import ru.ast.exceptions.AdminAlreadyExistException;
import ru.ast.exceptions.IncorrectLoginOrPasswordException;
import ru.ast.mapper.AdminMapper;
import ru.ast.repository.AdminRepository;
import ru.ast.security.JwtService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AdminResponse registerAdmin(AdminRegistrationRequest request) {

        String encodePassword = passwordEncoder.encode(request.password());
        // Проверяем, не занят ли email
        if (adminRepository.findByEmail(request.email())
                .isPresent()) {
            throw new AdminAlreadyExistException(String.format("Учетная запись: \"%s\" существует:", request.email()));
        }
        Admin admin = new Admin();
        admin.setName(request.name());
        admin.setSurname(request.surname());
        admin.setEmail(request.email());
        admin.setRole(Role.ADMIN);
        admin.setPasswordHash(encodePassword);
        admin.setCreatedAt(LocalDateTime.now());

        Admin saved = adminRepository.save(admin);
        log.info("✅ Зарегистрирован администратор: {}", saved.getUsername());
        return AdminMapper.toDto(saved);
    }

    /**
     * Аутентификация администратора
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Admin admin = adminRepository.findByEmail(request.getUsername())
                .orElseThrow(() -> new IncorrectLoginOrPasswordException("Неверное имя пользователя или пароль"));

        String token = jwtService.generateToken(admin);

        log.info("✅ Администратор {} вошёл в систему", admin.getName());

        return new AuthResponse(
                token,
                admin.getEmail(),
                admin.getRole().toString(),
                jwtService.getExpiration()
        );
    }

    public List<AdminResponse> getAllAdmins() {
        List<Admin> allAdmin = adminRepository.findAll();
        log.info("Получение списка администраторов");
        return allAdmin.stream()
                .map(AdminMapper::toDto)
                .toList();
    }
}
