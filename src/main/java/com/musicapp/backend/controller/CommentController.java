package com.musicapp.backend.controller;

import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.comment.CommentDto;
import com.musicapp.backend.dto.comment.CreateCommentRequest;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/songs/{songId}/comments")
    public ResponseEntity<BaseResponse<CommentDto>> createCommentForSong(
            @PathVariable Long songId,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateCommentRequest request) {

        CommentDto newComment = commentService.createCommentForSong(songId, currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse<>(true, "Bình luận đã được tạo thành công.", newComment));
    }

    @GetMapping("/songs/{songId}/comments")
    public ResponseEntity<BaseResponse<PagedResponse<CommentDto>>> getCommentsForSong(
            @PathVariable Long songId,
            @PageableDefault(size = 10) Pageable pageable) {

        PagedResponse<CommentDto> comments = commentService.getCommentsForSong(songId, pageable);
        return ResponseEntity.ok(new BaseResponse<>(true, "Lấy danh sách bình luận thành công.", comments));
    }

    @PostMapping("/playlists/{playlistId}/comments")
    public ResponseEntity<BaseResponse<CommentDto>> createCommentForPlaylist(
            @PathVariable Long playlistId,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateCommentRequest request) {

        CommentDto newComment = commentService.createCommentForPlaylist(playlistId, currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse<>(true, "Bình luận đã được tạo thành công.", newComment));
    }

    @GetMapping("/playlists/{playlistId}/comments")
    public ResponseEntity<BaseResponse<PagedResponse<CommentDto>>> getCommentsForPlaylist(
            @PathVariable Long playlistId,
            @PageableDefault(size = 10) Pageable pageable) {

        PagedResponse<CommentDto> comments = commentService.getCommentsForPlaylist(playlistId, pageable);
        return ResponseEntity.ok(new BaseResponse<>(true, "Lấy danh sách bình luận thành công.", comments));
    }

    @DeleteMapping("/songs/comments/{commentId}")
    public ResponseEntity<BaseResponse<Object>> deleteSongComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal User currentUser) {
        commentService.deleteSongComment(commentId, currentUser);
        return ResponseEntity.ok(new BaseResponse<>(true, "Đã xóa bình luận thành công.", null));
    }

    @DeleteMapping("/playlists/comments/{commentId}")
    public ResponseEntity<BaseResponse<Object>> deletePlaylistComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal User currentUser) {
        commentService.deletePlaylistComment(commentId, currentUser);
        return ResponseEntity.ok(new BaseResponse<>(true, "Đã xóa bình luận thành công.", null));
    }
}