// src/main/java/com/musicapp/backend/dto/user/ChangePasswordRequest.java
package com.musicapp.backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    // Đã đổi @NotBlank thành @NotEmpty để nhất quán, nhưng @NotBlank cũng hoạt động tốt.
    @NotBlank(message = "Mật khẩu hiện tại là bắt buộc.")
    private String currentPassword;

    @NotBlank(message = "Mật khẩu mới là bắt buộc.")
    @Size(min = 6, max = 32, message = "Mật khẩu mới phải có độ dài từ 6 đến 32 ký tự.")
    // Annotation @Pattern đã bị xóa để cho phép ký tự đặc biệt, theo chuẩn thông thường.
    // Nếu bạn muốn giữ lại, không sao cả.
    private String newPassword;

    @NotBlank(message = "Xác nhận mật khẩu là bắt buộc.")
    private String confirmationPassword;
}