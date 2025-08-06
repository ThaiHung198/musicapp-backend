package com.musicapp.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRequest {
    @NotEmpty(message = "Email là bắt buộc.")
    @Email(message = "Email không hợp lệ.")
    private String email;

    @NotEmpty(message = "Mật khẩu là bắt buộc.")
    private String password;
}
