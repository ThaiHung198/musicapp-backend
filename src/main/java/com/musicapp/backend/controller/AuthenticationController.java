package com.musicapp.backend.controller;

import com.musicapp.backend.dto.*;
import com.musicapp.backend.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<AuthenticationResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        AuthenticationResponse responseData = authenticationService.register(request);
        return ResponseEntity.ok(
                BaseResponse.success("Đăng ký tài khoản thành công!", responseData)
        );
    }

    @PostMapping("/authenticate")
    public ResponseEntity<BaseResponse<AuthenticationResponse>> authenticate(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        AuthenticationResponse responseData = authenticationService.authenticate(request);
        return ResponseEntity.ok(
                BaseResponse.success("Đăng nhập thành công!", responseData)
        );
    }

    @PostMapping("/google")
    public ResponseEntity<BaseResponse<AuthenticationResponse>> authenticateWithGoogle(
            @Valid @RequestBody GoogleLoginRequest request
    ) throws GeneralSecurityException, IOException {
        AuthenticationResponse responseData = authenticationService.loginWithGoogle(request);
        return ResponseEntity.ok(
                BaseResponse.success("Đăng nhập bằng Google thành công!", responseData)
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<BaseResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        authenticationService.handleForgotPassword(request);
        return ResponseEntity.ok(
                BaseResponse.success("Đã gửi mã OTP đến email của bạn. Vui lòng kiểm tra hộp thư.", null)
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<BaseResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        authenticationService.handleResetPassword(request);
        return ResponseEntity.ok(
                BaseResponse.success("Đặt lại mật khẩu thành công!", null)
        );
    }
    @PostMapping("/verify-otp")
    public ResponseEntity<BaseResponse<Void>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request
    ) {
        authenticationService.verifyOtp(request);
        return ResponseEntity.ok(
                BaseResponse.success("Mã OTP hợp lệ.", null)
        );
    }
}