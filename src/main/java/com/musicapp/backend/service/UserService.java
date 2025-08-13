package com.musicapp.backend.service;

import com.musicapp.backend.dto.PageInfo;
import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.user.AdminUserViewDto;
import com.musicapp.backend.dto.user.ChangePasswordRequest;
import com.musicapp.backend.dto.user.UpdateProfileRequest;
import com.musicapp.backend.dto.user.UserProfileDto;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.exception.BadRequestException;
import com.musicapp.backend.exception.ResourceNotFoundException;
import com.musicapp.backend.mapper.UserMapper;
import com.musicapp.backend.repository.RoleRepository;
import com.musicapp.backend.repository.SongRepository;
import com.musicapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.musicapp.backend.dto.creator.CreatorViewDto; // <<< IMPORT
import com.musicapp.backend.entity.Song; // <<< IMPORT
import com.musicapp.backend.entity.Role;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionService subscriptionService;
    private static final List<String> VALID_GENDERS = Arrays.asList("Male", "Female", "Other");
    private final RoleRepository roleRepository;
    private final SongRepository songRepository;

    @Transactional(readOnly = true)
    public UserProfileDto getCurrentUserProfile(User currentUser) {
        User user = userRepository.findByEmail(currentUser.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng."));
        return userMapper.toUserProfileDto(user);
    }

    public PagedResponse<AdminUserViewDto> getAllUsersForAdmin(String search, Pageable pageable) {
        Page<User> userPage;

        // 1. Lấy danh sách người dùng từ DB (có tìm kiếm)
        if (StringUtils.hasText(search)) {
            userPage = userRepository.findByDisplayNameContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        // 2. Chuyển đổi Page<User> thành Page<AdminUserViewDto>
        Page<AdminUserViewDto> dtoPage = userPage.map(user -> {
            // Xác định trạng thái cho mỗi user
            boolean isPremium = subscriptionService.hasActivePremiumSubscription(user.getId());
            String status = isPremium ? "PREMIUM" : "FREE";

            // Dùng mapper để tạo DTO
            return userMapper.toAdminUserViewDto(user, status);
        });

        // 3. Trả về kết quả dạng PagedResponse
        return PagedResponse.of(dtoPage.getContent(), dtoPage);
    }

    public PagedResponse<CreatorViewDto> getAllCreators(String search, Pageable pageable) {
        // 1. Lấy role CREATOR
        Role creatorRole = roleRepository.findByName("ROLE_CREATOR")
                .orElseThrow(() -> new ResourceNotFoundException("Vai trò 'ROLE_CREATOR' không tồn tại."));

        // 2. Lấy danh sách user là creator từ DB
        String keyword = (search == null) ? "" : search;
        Page<User> creatorPage = userRepository.findByRoleAndKeyword(creatorRole, keyword, pageable);

        // 3. Chuyển đổi sang DTO
        List<CreatorViewDto> dtos = creatorPage.getContent().stream().map(creator -> {
            CreatorViewDto dto = new CreatorViewDto();
            dto.setId(creator.getId());
            dto.setDisplayName(creator.getDisplayName());
            dto.setEmail(creator.getEmail());
            dto.setPhoneNumber(creator.getPhoneNumber());

            long count = songRepository.countByCreatorIdAndStatus(creator.getId(), Song.SongStatus.APPROVED);
            dto.setApprovedSongCount(count);

            return dto;
        }).collect(Collectors.toList());

        // 4. Trả về kết quả
        return new PagedResponse<>(dtos, new PageInfo(creatorPage));
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