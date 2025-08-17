package com.musicapp.backend.dto.user;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @NotEmpty(message = "Tên hiển thị không được để trống.")
    private String displayName;

    @Pattern(regexp = "(^$|[0-9]{10,11})", message = "Số điện thoại không hợp lệ.")
    private String phoneNumber;

    private LocalDate dateOfBirth;

    @Pattern(regexp = "(^$|Male|Female|Other)", message = "Giới tính không hợp lệ.")
    private String gender;

    private boolean removeAvatar;
}