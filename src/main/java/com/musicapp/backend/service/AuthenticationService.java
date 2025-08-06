// src/main/java/com/musicapp/backend/service/AuthenticationService.java
package com.musicapp.backend.service;

import com.musicapp.backend.dto.AuthenticationRequest;
import com.musicapp.backend.dto.AuthenticationResponse;
import com.musicapp.backend.dto.RegisterRequest;
import com.musicapp.backend.entity.Role;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.exception.ResourceAlreadyExistsException;
import com.musicapp.backend.exception.ResourceNotFoundException;
import com.musicapp.backend.repository.RoleRepository;
import com.musicapp.backend.repository.UserRepository;
import com.musicapp.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * <<< ĐÃ SỬA: Chuyển kiểu trả về thành 'void'.
     * Phương thức này chỉ chịu trách nhiệm tạo người dùng, không cần trả về gì cả.
     */
    public void register(RegisterRequest request) {
        userRepository.findByEmail(request.getEmail())
                .ifPresent(user -> {
                    throw new ResourceAlreadyExistsException("Email đã tồn tại.");
                });

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Vai trò 'ROLE_USER' không tồn tại trong database."));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        var user = User.builder()
                .displayName(request.getDisplayName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .build();

        userRepository.save(user);

        // Không còn câu lệnh return ở đây
    }

    /**
     * Phương thức này không đổi, vẫn trả về token khi đăng nhập thành công.
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng."));
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }
}