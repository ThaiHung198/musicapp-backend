package com.musicapp.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GoogleLoginRequest {

    @NotBlank(message = "Google ID token không được để trống.")
    private String idToken;
}