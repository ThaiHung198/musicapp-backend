package com.musicapp.backend.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.musicapp.backend.dto.*;
import com.musicapp.backend.entity.Role;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.exception.BadRequestException;
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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final EmailService emailService;

    public AuthenticationResponse register(RegisterRequest request) {
        userRepository.findByEmail(request.getEmail())
                .ifPresent(user -> {
                    throw new ResourceAlreadyExistsException("Email đã tồn tại.");
                });

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Vai trò 'ROLE_USER' không tồn tại trong database. Vui lòng thêm vai trò này."));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        var user = User.builder()
                .displayName(request.getDisplayName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .build();

        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

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
        User user = processOAuth2User(payload);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }

    private User processOAuth2User(GoogleIdToken.Payload payload) {
        return userRepository.findByEmail(payload.getEmail())
                .orElseGet(() -> {
                    Role userRole = roleRepository.findByName("ROLE_USER")
                            .orElseThrow(() -> new ResourceNotFoundException("Vai trò 'ROLE_USER' không tồn tại."));
                    User newUser = User.builder()
                            .email(payload.getEmail())
                            .displayName((String) payload.get("name"))
                            .avatarPath((String) payload.get("picture"))
                            .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                            .provider("google")
                            .roles(new HashSet<>(Collections.singletonList(userRole)))
                            .build();
                    return userRepository.save(newUser);
                });
    }

    public void handleForgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với email này."));

        String otp = String.format("%06d", ThreadLocalRandom.current().nextInt(100000, 1000000));
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);

        user.setOtpCode(otp);
        user.setOtpExpirationTime(expirationTime);
        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), otp);
    }

    public void verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với email này."));

        if (user.getOtpCode() == null || !user.getOtpCode().equals(request.getOtpCode())) {
            throw new BadRequestException("Mã OTP không hợp lệ.");
        }

        if (LocalDateTime.now().isAfter(user.getOtpExpirationTime())) {
            throw new BadRequestException("Mã OTP đã hết hạn.");
        }
    }

    public void handleResetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với email này."));

        if (user.getOtpCode() == null || !user.getOtpCode().equals(request.getOtpCode())) {
            throw new BadRequestException("Mã OTP không hợp lệ.");
        }

        if (LocalDateTime.now().isAfter(user.getOtpExpirationTime())) {
            throw new BadRequestException("Mã OTP đã hết hạn.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setOtpCode(null);
        user.setOtpExpirationTime(null);
        userRepository.save(user);
    }
}