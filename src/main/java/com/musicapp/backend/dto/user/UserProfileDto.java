package com.musicapp.backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private String displayName;
    private String email;
    private String phoneNumber;
    private String avatarPath;
    private LocalDate dateOfBirth;
    private String gender;
    private String provider;
    private LocalDateTime createdAt;
    private List<String> roles;
}
