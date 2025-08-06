// src/main/java/com/musicapp/backend/service/UserService.java
package com.musicapp.backend.service;

import com.musicapp.backend.dto.user.ChangePasswordRequest;
import com.musicapp.backend.dto.user.UpdateProfileRequest;
import com.musicapp.backend.dto.user.UserProfileDto;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.exception.BadRequestException;
import com.musicapp.backend.mapper.UserMapper;
import com.musicapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

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
    @Transactional
    public void changePassword(User currentUser, ChangePasswordRequest request) {
        // 1. Kiểm tra mật khẩu mới và mật khẩu xác nhận có khớp nhau không.
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new BadRequestException("Mật khẩu mới và mật khẩu xác nhận không khớp.");
        }

        // 2. Kiểm tra mật khẩu hiện tại có đúng không.
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new BadRequestException("Mật khẩu hiện tại không chính xác.");
        }

        // 3. Kiểm tra mật khẩu mới có trùng với mật khẩu cũ không.
        if (passwordEncoder.matches(request.getNewPassword(), currentUser.getPassword())) {
            throw new BadRequestException("Mật khẩu mới không được trùng với mật khẩu cũ.");
        }

        // Nếu tất cả kiểm tra đều qua, tiến hành cập nhật mật khẩu
        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        currentUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(currentUser);
    }

}