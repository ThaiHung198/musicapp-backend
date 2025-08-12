package com.musicapp.backend.dto.singer;

import jakarta.validation.constraints.Email;
import lombok.Data;

// DTO này không yêu cầu @NotBlank vì admin có thể chỉ muốn cập nhật 1 trong các trường
@Data
public class AdminUpdateSingerRequest {

    private String name;

    @Email(message = "Email không hợp lệ")
    private String email;
}