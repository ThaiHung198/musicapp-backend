package com.musicapp.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    // <<< Khớp với cột display_name
    @NotEmpty(message = "Tên hiển thị là bắt buộc.")
    private String displayName;

    @NotEmpty(message = "Email là bắt buộc.")
    @Email(message = "Email không hợp lệ.")
    private String email;

    @NotEmpty(message = "Mật khẩu là bắt buộc.")
    @Size(min = 6, max = 32, message = "Mật khẩu phải có độ dài từ 6 đến 32 ký tự.")
    private String password;
}