package com.musicapp.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.dto.playlist.AddSongsToPlaylistRequest;
import com.musicapp.backend.dto.playlist.CreatePlaylistRequest;
import com.musicapp.backend.dto.playlist.PlaylistDto;
import com.musicapp.backend.dto.playlist.UpdatePlaylistRequest;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.service.PlaylistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    public ResponseEntity<BaseResponse<PlaylistDto>> createPlaylist(
            @RequestPart("request") String requestJson,
            @RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
            @AuthenticationPrincipal User currentUser
    ) throws JsonProcessingException {
        CreatePlaylistRequest request = new ObjectMapper().readValue(requestJson, CreatePlaylistRequest.class);
        PlaylistDto newPlaylist = playlistService.createPlaylist(request, thumbnailFile, currentUser);
        return ResponseEntity.ok(BaseResponse.success("Tạo playlist thành công!", newPlaylist));
    }


    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<PlaylistDto>> getPlaylistById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        PlaylistDto playlistDto = playlistService.getPlaylistById(id, currentUser);
        return ResponseEntity.ok(BaseResponse.success("Lấy thông tin playlist thành công!", playlistDto));
    }

    @GetMapping("/my-playlists")
    public ResponseEntity<BaseResponse<List<PlaylistDto>>> getMyPlaylists(
            @AuthenticationPrincipal User currentUser
    ) {
        List<PlaylistDto> myPlaylists = playlistService.getMyPlaylists(currentUser);
        return ResponseEntity.ok(BaseResponse.success("Lấy danh sách playlist thành công!", myPlaylists));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    public ResponseEntity<BaseResponse<PlaylistDto>> updatePlaylist(
            @PathVariable Long id,
            @RequestPart("request") String requestJson,
            @RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
            @AuthenticationPrincipal User currentUser
    ) throws JsonProcessingException {
        UpdatePlaylistRequest request = new ObjectMapper().readValue(requestJson, UpdatePlaylistRequest.class);
        PlaylistDto updatedPlaylist = playlistService.updatePlaylist(id, request, thumbnailFile, currentUser);
        return ResponseEntity.ok(BaseResponse.success("Cập nhật playlist thành công!", updatedPlaylist));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deletePlaylist(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        playlistService.deletePlaylist(id, currentUser);
        return ResponseEntity.ok(BaseResponse.success("Xóa playlist thành công!", null));
    }

    @PostMapping("/{id}/songs")
    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    public ResponseEntity<BaseResponse<PlaylistDto>> addSongsToPlaylist(
            @PathVariable Long id,
            @Valid @RequestBody AddSongsToPlaylistRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        PlaylistDto updatedPlaylist = playlistService.addSongsToPlaylist(id, request, currentUser);
        return ResponseEntity.ok(BaseResponse.success("Thêm bài hát vào playlist thành công!", updatedPlaylist));
    }

    @DeleteMapping("/{playlistId}/songs/{songId}")
    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    public ResponseEntity<BaseResponse<PlaylistDto>> removeSongFromPlaylist(
            @PathVariable Long playlistId,
            @PathVariable Long songId,
            @AuthenticationPrincipal User currentUser
    ) {
        PlaylistDto updatedPlaylist = playlistService.removeSongFromPlaylist(playlistId, songId, currentUser);
        return ResponseEntity.ok(BaseResponse.success("Xóa bài hát khỏi playlist thành công!", updatedPlaylist));
    }

    @PatchMapping("/{id}/toggle-visibility")
    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    public ResponseEntity<BaseResponse<PlaylistDto>> togglePlaylistVisibility(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        PlaylistDto updatedPlaylist = playlistService.togglePlaylistVisibility(id, currentUser);
        String message = "Trạng thái hiển thị của playlist đã được cập nhật thành " + updatedPlaylist.getVisibility();
        return ResponseEntity.ok(BaseResponse.success(message, updatedPlaylist));
    }
}