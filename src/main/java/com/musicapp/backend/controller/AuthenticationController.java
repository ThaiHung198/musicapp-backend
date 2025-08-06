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

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    /**
     * <<< ĐÃ SỬA: Kiểu dữ liệu trong BaseResponse là Void.
     * Controller gọi service (không nhận lại giá trị) và tự xây dựng response.
     */
    @PostMapping("/register")
    public ResponseEntity<BaseResponse<Void>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        // Gọi service để thực hiện hành động
        authenticationService.register(request);

        // Controller tự xây dựng response thành công
        return ResponseEntity.ok(
                BaseResponse.success("Đăng ký tài khoản thành công!", null)
        );
    }

    /**
     * Phương thức này không đổi.
     */
    @PostMapping("/authenticate")
    public ResponseEntity<BaseResponse<AuthenticationResponse>> authenticate(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        AuthenticationResponse responseData = authenticationService.authenticate(request);
        return ResponseEntity.ok(
                BaseResponse.success("Đăng nhập thành công!", responseData)
        );
    }
}