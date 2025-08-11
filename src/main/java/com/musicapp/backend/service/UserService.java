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
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private static final List<String> VALID_GENDERS = Arrays.asList("Male", "Female", "Other");

    public UserProfileDto getCurrentUserProfile(User currentUser) {
        return userMapper.toUserProfileDto(currentUser);
    }

    @Transactional
    public UserProfileDto updateCurrentUserProfile(User currentUser, UpdateProfileRequest request) {
        currentUser.setDisplayName(request.getDisplayName());

        if (StringUtils.hasText(request.getPhoneNumber())) {
            if (!request.getPhoneNumber().matches("^[0-9]{10,11}$")) {
                throw new BadRequestException("Số điện thoại không hợp lệ.");
            }
            currentUser.setPhoneNumber(request.getPhoneNumber());
        } else {
            currentUser.setPhoneNumber(null);
        }

        if (StringUtils.hasText(request.getGender())) {
            if (!VALID_GENDERS.contains(request.getGender())) {
                throw new BadRequestException("Giới tính phải là Male, Female, hoặc Other.");
            }
            currentUser.setGender(request.getGender());
        } else {
            currentUser.setGender(null);
        }

        currentUser.setDateOfBirth(request.getDateOfBirth());
        currentUser.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(currentUser);
        return userMapper.toUserProfileDto(updatedUser);
    }

    @Transactional
    public void changePassword(User currentUser, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new BadRequestException("Mật khẩu mới và mật khẩu xác nhận không khớp.");
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new BadRequestException("Mật khẩu hiện tại không chính xác.");
        }
        if (passwordEncoder.matches(request.getNewPassword(), currentUser.getPassword())) {
            throw new BadRequestException("Mật khẩu mới không được trùng với mật khẩu cũ.");
        }
        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        currentUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(currentUser);
    }
}