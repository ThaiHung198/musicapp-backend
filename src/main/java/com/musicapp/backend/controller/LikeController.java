package com.musicapp.backend.controller;

import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/likes")
@RequiredArgsConstructor
public class LikeController {
    
    private final LikeService likeService;
    
    @PostMapping("/songs/{songId}")
    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    public ResponseEntity<BaseResponse<Boolean>> toggleSongLike(
            @PathVariable Long songId,
            @AuthenticationPrincipal User user) {
        boolean isLiked = likeService.toggleSongLike(songId, user);
        String message = isLiked ? "Song liked successfully" : "Song unliked successfully";
        return ResponseEntity.ok(BaseResponse.success(message, isLiked));
    }
    
    @PostMapping("/playlists/{playlistId}")
    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    public ResponseEntity<BaseResponse<Boolean>> togglePlaylistLike(
            @PathVariable Long playlistId,
            @AuthenticationPrincipal User user) {
        boolean isLiked = likeService.togglePlaylistLike(playlistId, user);
        String message = isLiked ? "Playlist liked successfully" : "Playlist unliked successfully";
        return ResponseEntity.ok(BaseResponse.success(message, isLiked));
    }
    
    @GetMapping("/songs/{songId}/count")
    public ResponseEntity<BaseResponse<Long>> getSongLikeCount(@PathVariable Long songId) {
        long count = likeService.getSongLikeCount(songId);
        return ResponseEntity.ok(BaseResponse.success(count));
    }
    
    @GetMapping("/playlists/{playlistId}/count")
    public ResponseEntity<BaseResponse<Long>> getPlaylistLikeCount(@PathVariable Long playlistId) {
        long count = likeService.getPlaylistLikeCount(playlistId);
        return ResponseEntity.ok(BaseResponse.success(count));
    }
    
    @GetMapping("/songs/{songId}/status")
    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    public ResponseEntity<BaseResponse<Boolean>> getSongLikeStatus(
            @PathVariable Long songId,
            @AuthenticationPrincipal User user) {
        boolean isLiked = likeService.isSongLikedByUser(songId, user.getId());
        return ResponseEntity.ok(BaseResponse.success(isLiked));
    }
    
    @GetMapping("/playlists/{playlistId}/status")
    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    public ResponseEntity<BaseResponse<Boolean>> getPlaylistLikeStatus(
            @PathVariable Long playlistId,
            @AuthenticationPrincipal User user) {
        boolean isLiked = likeService.isPlaylistLikedByUser(playlistId, user.getId());
        return ResponseEntity.ok(BaseResponse.success(isLiked));
    }
}
