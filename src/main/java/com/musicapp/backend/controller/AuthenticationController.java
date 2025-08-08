// src/main/java/com/musicapp/backend/controller/AuthenticationController.java
package com.musicapp.backend.controller;

import com.musicapp.backend.dto.AuthenticationRequest;
import com.musicapp.backend.dto.AuthenticationResponse;
import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.dto.RegisterRequest;
import com.musicapp.backend.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.musicapp.backend.dto.GoogleLoginRequest;
import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    /**
     * <<< ĐÃ SỬA: Cập nhật kiểu trả về để chứa token sau khi đăng ký thành công.
     */
    @PostMapping("/register")
    public ResponseEntity<BaseResponse<AuthenticationResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        // Gọi service để thực hiện hành động, service sẽ trả về response chứa token
        AuthenticationResponse responseData = authenticationService.register(request);

        // Controller xây dựng response thành công và gửi về cho client
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
}