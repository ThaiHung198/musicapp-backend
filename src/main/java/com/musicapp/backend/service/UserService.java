// src/main/java/com/musicapp/backend/service/UserService.java
package com.musicapp.backend.service;

import com.musicapp.backend.dto.user.UpdateProfileRequest;
import com.musicapp.backend.dto.user.UserProfileDto;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.mapper.UserMapper;
import com.musicapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Lấy thông tin profile của người dùng đang đăng nhập.
     * @param currentUser Người dùng được lấy từ Security Context.
     * @return DTO chứa thông tin profile.
     */
    public UserProfileDto getCurrentUserProfile(User currentUser) {
        return userMapper.toUserProfileDto(currentUser);
    }

    /**
     * Cập nhật thông tin profile của người dùng đang đăng nhập.
     * @param currentUser Người dùng được lấy từ Security Context.
     * @param request DTO chứa thông tin cần cập nhật.
     * @return DTO chứa thông tin profile sau khi đã cập nhật.
     */
    @Transactional
    public UserProfileDto updateCurrentUserProfile(User currentUser, UpdateProfileRequest request) {
        // Cập nhật các trường từ request
        currentUser.setDisplayName(request.getDisplayName());
        currentUser.setPhoneNumber(request.getPhoneNumber());
        currentUser.setDateOfBirth(request.getDateOfBirth());
        currentUser.setGender(request.getGender());

        // Cập nhật thời gian updatedAt
        currentUser.setUpdatedAt(LocalDateTime.now());

        // Lưu lại vào database
        User updatedUser = userRepository.save(currentUser);

        // Trả về DTO của user đã được cập nhật
        return userMapper.toUserProfileDto(updatedUser);
    }
}