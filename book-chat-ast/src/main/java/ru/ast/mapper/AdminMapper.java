package ru.ast.mapper;


import ru.ast.dto.response.AdminResponse;
import ru.ast.entity.Admin;

public class AdminMapper {

    public static AdminResponse toDto(Admin admin) {
        AdminResponse dto = new AdminResponse();
        dto.setId(admin.getId());
        dto.setUsername(admin.getUsername());
        dto.setEmail(admin.getEmail());
        dto.setRole(admin.getRole().toString());
        dto.setCreatedAt(admin.getCreatedAt());

        return dto;
    }
}
