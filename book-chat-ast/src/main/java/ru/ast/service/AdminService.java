package ru.ast.service;


import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.ast.dto.AdminDto;
import ru.ast.dto.request.RegisterAdminRequest;
import ru.ast.entity.Admin;
import ru.ast.enums.Role;
import ru.ast.exceptions.AdminAlreadyExistException;
import ru.ast.mapper.AdminMapper;
import ru.ast.repository.AdminRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AdminDto createAdmin(RegisterAdminRequest request) {
        Admin admin = new Admin();

        String encodePassword = passwordEncoder.encode(request.password());
        if (adminRepository.findByEmail(request.email())
                .isPresent()) {
            throw new AdminAlreadyExistException(String.format("Учетная запись: \"%s\" существует:", request.email()));
        }
        admin.setName(request.name());
        admin.setSurname(request.surname());
        admin.setEmail(request.email());
        admin.setRole(Role.ROLE_ADMIN);
        admin.setPasswordHash(encodePassword);

        Admin saved = adminRepository.save(admin);

        return AdminMapper.toDto(saved);
    }

    public List<AdminDto> getAllAdmins() {
        return adminRepository.findAll()
                .stream()
                .map(AdminMapper::toDto)
                .toList();
    }
}
