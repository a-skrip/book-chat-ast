package ru.ast.service;

import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.ast.entity.Admin;
import ru.ast.exceptions.AdminNotFoundException;
import ru.ast.repository.AdminRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;


    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByName(name)
                .orElseThrow(() -> new AdminNotFoundException("Admin not found: " + name));

        return new org.springframework.security.core.userdetails.User(
                admin.getEmail(),
                admin.getPasswordHash(),
                List.of(new SimpleGrantedAuthority(admin.getRole().name())));

    }


}