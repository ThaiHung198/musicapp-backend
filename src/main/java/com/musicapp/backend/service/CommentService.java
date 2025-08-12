package com.musicapp.backend.service;

import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.PageInfo;
import com.musicapp.backend.dto.comment.CommentDto;
import com.musicapp.backend.dto.comment.CreateCommentRequest;
import com.musicapp.backend.entity.*;
import com.musicapp.backend.exception.ResourceNotFoundException;
import com.musicapp.backend.exception.UnauthorizedException;
import com.musicapp.backend.mapper.CommentMapper;
import com.musicapp.backend.repository.CommentRepository;
import com.musicapp.backend.repository.PlaylistRepository;
import com.musicapp.backend.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final SongRepository songRepository;
    private final PlaylistRepository playlistRepository;
    private final CommentMapper commentMapper;

    // --- Phương thức tạo bình luận ---

    @Transactional
    public CommentDto createCommentForSong(Long songId, User currentUser, CreateCommentRequest request) {
        // Kiểm tra xem bài hát có tồn tại không
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài hát với ID: " + songId));

        // Tạo đối tượng Comment
        Comment comment = buildComment(currentUser, request.getContent(), songId, Comment.CommentableType.SONG);

        // Lưu vào cơ sở dữ liệu
        Comment savedComment = commentRepository.save(comment);

        // Chuyển đổi sang DTO để trả về
        return commentMapper.toDto(savedComment);
    }

    @Transactional
    public CommentDto createCommentForPlaylist(Long playlistId, User currentUser, CreateCommentRequest request) {
        // Kiểm tra xem playlist có tồn tại không
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy playlist với ID: " + playlistId));

        // Tạo đối tượng Comment
        Comment comment = buildComment(currentUser, request.getContent(), playlistId, Comment.CommentableType.PLAYLIST);

        // Lưu vào cơ sở dữ liệu
        Comment savedComment = commentRepository.save(comment);

        // Chuyển đổi sang DTO để trả về
        return commentMapper.toDto(savedComment);
    }

    // --- Phương thức lấy danh sách bình luận ---

    public PagedResponse<CommentDto> getCommentsForSong(Long songId, Pageable pageable) {
        // Kiểm tra xem bài hát có tồn tại không
        if (!songRepository.existsById(songId)) {
            throw new ResourceNotFoundException("Không tìm thấy bài hát với ID: " + songId);
        }

        // Lấy trang bình luận từ repository
        Page<Comment> commentPage = commentRepository.findByCommentableIdAndCommentableTypeOrderByCreatedAtDesc(
                songId, Comment.CommentableType.SONG, pageable);

        return createPagedResponse(commentPage);
    }

    public PagedResponse<CommentDto> getCommentsForPlaylist(Long playlistId, Pageable pageable) {
        // Kiểm tra xem playlist có tồn tại không
        if (!playlistRepository.existsById(playlistId)) {
            throw new ResourceNotFoundException("Không tìm thấy playlist với ID: " + playlistId);
        }

        // Lấy trang bình luận từ repository
        Page<Comment> commentPage = commentRepository.findByCommentableIdAndCommentableTypeOrderByCreatedAtDesc(
                playlistId, Comment.CommentableType.PLAYLIST, pageable);

        return createPagedResponse(commentPage);
    }

    // --- Phương thức xóa bình luận ---

    @Transactional
    public void deleteComment(Long commentId, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bình luận với ID: " + commentId));

        // Chỉ chủ sở hữu bình luận mới có quyền xóa
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Bạn không có quyền xóa bình luận này.");
        }

        commentRepository.delete(comment);
    }

    // --- Các phương thức hỗ trợ (private) ---

    private Comment buildComment(User user, String content, Long commentableId, Comment.CommentableType type) {
        return Comment.builder()
                .user(user)
                .content(content)
                .commentableId(commentableId)
                .commentableType(type)
                .build();
    }

    private PagedResponse<CommentDto> createPagedResponse(Page<Comment> commentPage) {
        List<CommentDto> commentDtos = commentPage.getContent().stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());

        PageInfo pageInfo = new PageInfo(commentPage);

        return new PagedResponse<>(commentDtos, pageInfo);
    }
}