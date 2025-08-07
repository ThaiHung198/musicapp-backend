package com.musicapp.backend.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
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
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.musicapp.backend.dto.GoogleLoginRequest;
import com.musicapp.backend.exception.BadRequestException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;

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
    public AuthenticationResponse loginWithGoogle(GoogleLoginRequest request) throws GeneralSecurityException, IOException {
        GoogleIdToken idToken = googleIdTokenVerifier.verify(request.getIdToken());
        if (idToken == null) {
            throw new BadRequestException("Token Google không hợp lệ.");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();

        // Tìm hoặc tạo người dùng mới
        User user = processOAuth2User(payload);

        // Tạo JWT token của ứng dụng và trả về
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }

    private User processOAuth2User(GoogleIdToken.Payload payload) {
        Optional<User> userOptional = userRepository.findByEmail(payload.getEmail());

        if (userOptional.isPresent()) {
            // User đã tồn tại, trả về user đó
            return userOptional.get();
        } else {
            // User chưa tồn tại, tạo mới
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new ResourceNotFoundException("Vai trò 'ROLE_USER' không tồn tại."));

            User newUser = User.builder()
                    .email(payload.getEmail())
                    .displayName((String) payload.get("name"))
                    .avatarPath((String) payload.get("picture"))
                    // Mật khẩu ngẫu nhiên vì user sẽ không bao giờ dùng nó để đăng nhập
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .provider("google") // Đánh dấu là user từ Google
                    .roles(new HashSet<>(Collections.singletonList(userRole)))
                    .build();

            return userRepository.save(newUser);
        }
    }
}