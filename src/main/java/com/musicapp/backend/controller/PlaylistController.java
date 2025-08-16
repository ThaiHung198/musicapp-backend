// File: src/main/java/com/musicapp/backend/controller/PlaylistController.java
package com.musicapp.backend.controller;
import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.dto.playlist.*;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.service.PlaylistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/v1/playlists")
@RequiredArgsConstructor
public class PlaylistController {
    private final PlaylistService playlistService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<PlaylistDto>> createPlaylist(
            @Valid @RequestPart("playlistRequest") CreatePlaylistRequest request,
            @RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
            @AuthenticationPrincipal User currentUser) {
        PlaylistDto newPlaylist = playlistService.createPlaylist(request, thumbnailFile, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("Tạo playlist thành công.", newPlaylist));
    }

    @GetMapping("/my-playlists")
    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')") // Giữ lại fix từ lần trước
    public ResponseEntity<BaseResponse<List<PlaylistDto>>> getMyPlaylists(@AuthenticationPrincipal User currentUser) {
        List<PlaylistDto> playlists = playlistService.getMyPlaylists(currentUser);
        return ResponseEntity.ok(BaseResponse.success(playlists));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<Page<PlaylistDto>>> getAllPublicPlaylists(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal User currentUser) {
        Page<PlaylistDto> playlists = playlistService.getAllPublicPlaylists(pageable, currentUser);
        return ResponseEntity.ok(BaseResponse.success(playlists));
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<BaseResponse<PlaylistDetailDto>> getPlaylistById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        PlaylistDetailDto playlist = playlistService.getPlaylistById(id, currentUser);
        return ResponseEntity.ok(BaseResponse.success(playlist));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<PlaylistDetailDto>> updatePlaylist(
            @PathVariable Long id,
            @Valid @RequestPart("playlistRequest") UpdatePlaylistRequest request,
            @RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
            @AuthenticationPrincipal User currentUser
    ) {
        PlaylistDetailDto updatedPlaylist = playlistService.updatePlaylist(id, request, thumbnailFile, currentUser);
        return ResponseEntity.ok(BaseResponse.success("Cập nhật playlist thành công.", updatedPlaylist));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<Void>> deletePlaylist(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        playlistService.deletePlaylist(id, currentUser);
        return ResponseEntity.ok(BaseResponse.success("Xóa playlist thành công.", null));
    }

    @PostMapping("/{id}/songs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<PlaylistDto>> addSongsToPlaylist(
            @PathVariable Long id,
            @RequestBody AddSongsToPlaylistRequest request,
            @AuthenticationPrincipal User currentUser) {
        PlaylistDto updatedPlaylist = playlistService.addSongsToPlaylist(id, request, currentUser);
        return ResponseEntity.ok(BaseResponse.success("Thêm bài hát vào playlist thành công.", updatedPlaylist));
    }

    @DeleteMapping("/{playlistId}/songs/{songId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<PlaylistDto>> removeSongFromPlaylist(
            @PathVariable Long playlistId,
            @PathVariable Long songId,
            @AuthenticationPrincipal User currentUser) {
        PlaylistDto updatedPlaylist = playlistService.removeSongFromPlaylist(playlistId, songId, currentUser);
        return ResponseEntity.ok(BaseResponse.success("Xóa bài hát khỏi playlist thành công.", updatedPlaylist));
    }

    @PostMapping("/{id}/toggle-visibility")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<PlaylistDto>> togglePlaylistVisibility(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        PlaylistDto updatedPlaylist = playlistService.togglePlaylistVisibility(id, currentUser);
        String message = "PUBLIC".equals(updatedPlaylist.getVisibility()) ? "Playlist đã được công khai." : "Playlist đã được ẩn.";
        return ResponseEntity.ok(BaseResponse.success(message, updatedPlaylist));
    }

    @GetMapping("/admin/management")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<AdminPlaylistManagementDto>> getPlaylistsForAdminManagement(@AuthenticationPrincipal User admin) {
        AdminPlaylistManagementDto data = playlistService.getPlaylistsForAdminManagement(admin);
        return ResponseEntity.ok(BaseResponse.success(data));
    }

    @PostMapping("/{id}/increment-listen-count")
    @PreAuthorize("permitAll()")
    public ResponseEntity<BaseResponse<Void>> incrementListenCount(@PathVariable Long id) {
        playlistService.incrementListenCount(id);
        return ResponseEntity.ok(BaseResponse.success("Playlist listen count incremented.", null));
    }

    @GetMapping("/top-listened")
    @PreAuthorize("permitAll()")
    public ResponseEntity<BaseResponse<List<PlaylistDto>>> getTopListenedPlaylists(
            @RequestParam(defaultValue = "8") int limit,
            @AuthenticationPrincipal User currentUser) {
        List<PlaylistDto> playlists = playlistService.getTopListenedPlaylists(limit, currentUser);
        return ResponseEntity.ok(BaseResponse.success(playlists));
    }
}