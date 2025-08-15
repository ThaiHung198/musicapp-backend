package com.musicapp.backend.controller;

import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.user.*;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<PagedResponse<AdminUserViewDto>>> getAllUsersForAdmin(
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search) {
        PagedResponse<AdminUserViewDto> response = userService.getAllUsersForAdmin(search, pageable);
        return ResponseEntity.ok(BaseResponse.success("Lấy danh sách người dùng thành công.", response));
    }

    @PutMapping("/admin/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<AdminUserViewDto>> updateUserByAdmin(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserByAdminRequest request) {
        AdminUserViewDto updatedUser = userService.updateUserByAdmin(userId, request);
        return ResponseEntity.ok(BaseResponse.success("Cập nhật vai trò người dùng thành công.", updatedUser));
    }

    @PostMapping("/{userId}/promote-creator")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<UserProfileDto>> promoteCreator(@PathVariable Long userId) {
        UserProfileDto updatedUser = userService.promoteUserToCreator(userId);
        return ResponseEntity.ok(BaseResponse.success("Nâng cấp người dùng thành Creator thành công.", updatedUser));
    }

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserProfileDto>> getCurrentUserProfile(
            @AuthenticationPrincipal User currentUser
    ) {
        UserProfileDto userProfile = userService.getCurrentUserProfile(currentUser);
        return ResponseEntity.ok(BaseResponse.success("Lấy thông tin người dùng thành công.", userProfile));
    }

    @PutMapping(value = "/me", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<BaseResponse<UserProfileDto>> updateCurrentUserProfile(
            @AuthenticationPrincipal User currentUser,
            @RequestPart("profileData") @Valid UpdateProfileRequest request,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile
    ) {
        UserProfileDto updatedUserProfile = userService.updateCurrentUserProfile(currentUser, request, avatarFile);
        return ResponseEntity.ok(BaseResponse.success("Cập nhật thông tin thành công.", updatedUserProfile));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<BaseResponse<Void>> changePassword(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(currentUser, request);
        return ResponseEntity.ok(BaseResponse.success("Đổi mật khẩu thành công. Vui lòng đăng nhập lại.", null));
    }
}