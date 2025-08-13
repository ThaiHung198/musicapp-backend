package com.musicapp.backend.dto.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.Set;

@Data
public class UpdateUserByAdminRequest {

    @NotEmpty(message = "Danh sách vai trò không được để trống.")
    private Set<String> roles;

}