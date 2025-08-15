package com.musicapp.backend.service;

import com.musicapp.backend.dto.PageInfo;
import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.creator.CreatorDetailDto;
import com.musicapp.backend.dto.user.*;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.exception.BadRequestException;
import com.musicapp.backend.exception.ResourceNotFoundException;
import com.musicapp.backend.mapper.SongMapper;
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
import org.springframework.web.multipart.MultipartFile;
import com.musicapp.backend.dto.creator.CreatorViewDto;
import com.musicapp.backend.entity.Song;
import com.musicapp.backend.entity.Role;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.musicapp.backend.repository.PlaylistRepository;
import com.musicapp.backend.entity.Playlist;

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
    private final PlaylistRepository playlistRepository;
    private final SongMapper songMapper;
    private final FileStorageService fileStorageService;


    @Transactional(readOnly = true)
    public UserProfileDto getCurrentUserProfile(User currentUser) {
        User user = userRepository.findByEmail(currentUser.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng."));
        return userMapper.toUserProfileDto(user);
    }

    public PagedResponse<AdminUserViewDto> getAllUsersForAdmin(String search, Pageable pageable) {
        String keyword = (search == null) ? "" : search.trim();

        Page<User> userPage = userRepository.findAppUsers(keyword, pageable);

        Page<AdminUserViewDto> dtoPage = userPage.map(user -> {
            boolean isPremium = subscriptionService.hasActivePremiumSubscription(user.getId());
            return userMapper.toAdminUserViewDto(user, isPremium);
        });

        return PagedResponse.of(dtoPage.getContent(), dtoPage);
    }

    @Transactional
    public AdminUserViewDto updateUserByAdmin(Long userId, UpdateUserByAdminRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        Role creatorRole = roleRepository.findByName("ROLE_CREATOR")
                .orElseThrow(() -> new ResourceNotFoundException("Vai trò 'ROLE_CREATOR' không tồn tại."));

        boolean wasCreator = user.getRoles().contains(creatorRole);
        boolean isNowCreator = request.getRoles().contains("ROLE_CREATOR");

        if (!wasCreator && isNowCreator) {
            playlistRepository.deleteByCreatorIdAndVisibility(user.getId(), Playlist.PlaylistVisibility.PRIVATE);
        }

        Set<Role> newRoles = request.getRoles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new BadRequestException("Vai trò không hợp lệ: " + roleName)))
                .collect(Collectors.toSet());
        user.setRoles(newRoles);

        User updatedUser = userRepository.save(user);

        boolean isPremium = subscriptionService.hasActivePremiumSubscription(updatedUser.getId());
        return userMapper.toAdminUserViewDto(updatedUser, isPremium);
    }

    public PagedResponse<CreatorViewDto> getAllCreators(String search, Pageable pageable) {
        String keyword = (search == null) ? "" : search;
        Page<User> creatorPage = userRepository.findByRoleNameAndKeyword("ROLE_CREATOR", keyword, pageable);

        Page<CreatorViewDto> dtoPage = creatorPage.map(creator -> {
            CreatorViewDto dto = new CreatorViewDto();
            dto.setId(creator.getId());
            dto.setDisplayName(creator.getDisplayName());
            dto.setEmail(creator.getEmail());
            dto.setPhoneNumber(creator.getPhoneNumber());

            long count = songRepository.countByCreatorIdAndStatus(creator.getId(), Song.SongStatus.APPROVED);
            dto.setApprovedSongCount(count);

            return dto;
        });

        return PagedResponse.of(dtoPage.getContent(), dtoPage);
    }

    @Transactional(readOnly = true)
    public CreatorDetailDto getCreatorDetails(Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà phát triển với ID: " + creatorId));

        Role creatorRole = roleRepository.findByName("ROLE_CREATOR")
                .orElseThrow(() -> new ResourceNotFoundException("Vai trò 'ROLE_CREATOR' không tồn tại."));
        if (!creator.getRoles().contains(creatorRole)) {
            throw new BadRequestException("Người dùng này không phải là một Creator.");
        }

        List<Song> songs = songRepository.findByCreatorId(creatorId);

        return userMapper.toCreatorDetailDto(creator, songs);
    }

    @Transactional
    public UserProfileDto promoteUserToCreator(Long userId) {
        Role creatorRole = roleRepository.findByName("ROLE_CREATOR")
                .orElseThrow(() -> new ResourceNotFoundException("Vai trò 'ROLE_CREATOR' không tồn tại trong database."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        boolean isAlreadyCreator = user.getRoles().contains(creatorRole);
        if (isAlreadyCreator) {
            throw new BadRequestException("Người dùng này đã là Creator.");
        }

        playlistRepository.deleteByCreatorIdAndVisibility(user.getId(), Playlist.PlaylistVisibility.PRIVATE);

        user.getRoles().add(creatorRole);

        User updatedUser = userRepository.save(user);
        return userMapper.toUserProfileDto(updatedUser);
    }

    @Transactional
    public UserProfileDto updateCurrentUserProfile(User currentUser, UpdateProfileRequest request, MultipartFile avatarFile) {
        // 1. Xử lý xóa avatar
        if (request.isRemoveAvatar()) {
            // (Tùy chọn) Xóa file vật lý nếu cần
            // fileStorageService.deleteFile(currentUser.getAvatarPath());
            currentUser.setAvatarPath(null);
        }
        else if (avatarFile != null && !avatarFile.isEmpty()) {
            String fileName = fileStorageService.storeFile(avatarFile, "images");
            currentUser.setAvatarPath(fileName);
        }

        // 2. Cập nhật các thông tin khác (logic này bạn đã có)
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

        // 3. Lưu lại vào database
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