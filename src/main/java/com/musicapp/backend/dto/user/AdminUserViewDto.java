package com.musicapp.backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserViewDto {
    private Long id;
    private String displayName;
    private String email;
    private String phoneNumber;
    private String premiumStatus; // "PREMIUM" hoặc "FREE"
    private String status; // "ACTIVE" hoặc "LOCKED"
    private List<String> roles; // Ví dụ: ["ROLE_USER", "ROLE_CREATOR"]
}