package com.musicapp.backend.controller;

import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.user.AdminUserViewDto;
import com.musicapp.backend.dto.user.ChangePasswordRequest;
import com.musicapp.backend.dto.user.UpdateProfileRequest;
import com.musicapp.backend.dto.user.UserProfileDto;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<PagedResponse<AdminUserViewDto>>> getAllUsersForAdmin(
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search) {
        PagedResponse<AdminUserViewDto> response = userService.getAllUsersForAdmin(search, pageable);
        return ResponseEntity.ok(BaseResponse.success("Lấy danh sách người dùng thành công.", response));
    }

    @PostMapping("/{userId}/promote-creator")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<UserProfileDto>> promoteCreator(@PathVariable Long userId) {
        UserProfileDto updatedUser = userService.promoteUserToCreator(userId);
        return ResponseEntity.ok(BaseResponse.success("Nâng cấp người dùng thành Creator thành công.", updatedUser));
    }

    /**
     * API để lấy thông tin profile của người dùng đang đăng nhập.
     * @param currentUser Spring Security sẽ tự động inject user đang đăng nhập vào đây.
     * @return Thông tin profile.
     */
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserProfileDto>> getCurrentUserProfile(
            @AuthenticationPrincipal User currentUser
    ) {
        UserProfileDto userProfile = userService.getCurrentUserProfile(currentUser);
        return ResponseEntity.ok(BaseResponse.success("Lấy thông tin người dùng thành công.", userProfile));
    }

    /**
     * API để cập nhật thông tin profile của người dùng đang đăng nhập.
     * @param currentUser Spring Security sẽ tự động inject user đang đăng nhập.
     * @param request Dữ liệu mới cần cập nhật.
     * @return Thông tin profile sau khi đã cập nhật.
     */
    @PutMapping("/me")
    public ResponseEntity<BaseResponse<UserProfileDto>> updateCurrentUserProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserProfileDto updatedUserProfile = userService.updateCurrentUserProfile(currentUser, request);
        return ResponseEntity.ok(BaseResponse.success("Cập nhật thông tin thành công.", updatedUserProfile));
    }
    /**
     * API để người dùng thay đổi mật khẩu.
     */
    @PatchMapping("/me/password")
    public ResponseEntity<BaseResponse<Void>> changePassword(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(currentUser, request);
        return ResponseEntity.ok(BaseResponse.success("Đổi mật khẩu thành công. Vui lòng đăng nhập lại.", null));
    }
}