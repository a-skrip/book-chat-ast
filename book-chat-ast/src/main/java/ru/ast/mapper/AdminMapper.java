package ru.ast.mapper;


import ru.ast.dto.AdminDto;
import ru.ast.entity.Admin;

public class AdminMapper {

    public static AdminDto toDto(Admin admin) {
        AdminDto dto = new AdminDto();
        dto.setId(admin.getId());
        dto.setName(admin.getName());
        dto.setSurname(admin.getSurname());
        dto.setEmail(admin.getEmail());
        dto.setRole(admin.getRole().toString());

        return dto;
    }
}
