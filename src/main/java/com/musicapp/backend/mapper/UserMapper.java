// src/main/java/com/musicapp/backend/mapper/UserMapper.java
package com.musicapp.backend.mapper;

import com.musicapp.backend.dto.user.UserProfileDto;
import com.musicapp.backend.dto.user.AdminUserViewDto;
import com.musicapp.backend.entity.Role;
import com.musicapp.backend.entity.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserProfileDto toUserProfileDto(User user) {
        if (user == null) {
            return null;
        }

        return UserProfileDto.builder()
                .id(user.getId())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .avatarPath(user.getAvatarPath())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .provider(user.getProvider())
                .createdAt(user.getCreatedAt())
                // Chuyển từ Set<Role> sang List<String>
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()))
                .build();
    }

    public AdminUserViewDto toAdminUserViewDto(User user, String status) {
        if (user == null) {
            return null;
        }

        return AdminUserViewDto.builder()
                .id(user.getId())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .status(status) // Trạng thái được truyền từ service
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()))
                .build();
    }
}