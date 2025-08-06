// src/main/java/com/musicapp/backend/dto/user/UpdateProfileRequest.java
package com.musicapp.backend.dto.user;

import jakarta.validation.constraints.NotEmpty; // <<< THÊM IMPORT
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

    // <<< THÊM ANNOTATION BẮT BUỘC
    @NotEmpty(message = "Tên hiển thị không được để trống.")
    private String displayName;

    // Email không cần vì không sửa được, chúng ta sẽ bỏ qua nó trong logic service

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải có 10-11 chữ số.")
    private String phoneNumber;

    // Avatar sẽ được xử lý trong một API riêng (upload file), tạm thời bỏ qua

    private LocalDate dateOfBirth;

    @Pattern(regexp = "^(Male|Female|Other)$", message = "Giới tính phải là Male, Female, hoặc Other.")
    private String gender;
}