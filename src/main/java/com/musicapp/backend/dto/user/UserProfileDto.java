package com.musicapp.backend.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    private String subscriptionStatus;
    private LocalDate subscriptionEndDate;
    private boolean hasActiveSubscription;
}