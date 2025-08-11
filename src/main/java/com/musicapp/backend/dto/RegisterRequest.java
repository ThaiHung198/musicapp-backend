package com.musicapp.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
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

    @NotEmpty(message = "Tên hiển thị là bắt buộc.")
    @Size(min = 2, message = "Tên hiển thị phải có ít nhất 2 ký tự.")
    private String displayName;

    @NotEmpty(message = "Email là bắt buộc.")
    @Email(message = "Email không hợp lệ.")
    private String email;

    @NotEmpty(message = "Số điện thoại là bắt buộc.")
    @Pattern(regexp = "(84|0[3|5|7|8|9])+([0-9]{8})\\b", message = "Số điện thoại không hợp lệ.")
    private String phoneNumber;

    @NotEmpty(message = "Mật khẩu là bắt buộc.")
    @Size(min = 6, max = 32, message = "Mật khẩu phải có độ dài từ 6 đến 32 ký tự.")
    private String password;
}