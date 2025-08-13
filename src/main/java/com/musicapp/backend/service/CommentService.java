package com.musicapp.backend.service;

import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.PageInfo;
import com.musicapp.backend.dto.comment.CommentDto;
import com.musicapp.backend.dto.comment.CreateCommentRequest;
import com.musicapp.backend.entity.*;
import com.musicapp.backend.exception.ResourceNotFoundException;
import com.musicapp.backend.exception.UnauthorizedException;
import com.musicapp.backend.mapper.CommentMapper;
import com.musicapp.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final SongRepository songRepository;
    private final PlaylistRepository playlistRepository;
    private final SongCommentRepository songCommentRepository;
    private final PlaylistCommentRepository playlistCommentRepository;
    private final CommentMapper commentMapper;

    @Transactional
    public CommentDto createCommentForSong(Long songId, User currentUser, CreateCommentRequest request) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài hát với ID: " + songId));

        SongComment comment = new SongComment(currentUser, request.getContent(), song);
        SongComment savedComment = songCommentRepository.save(comment);

        return commentMapper.toDto(savedComment);
    }

    @Transactional
    public CommentDto createCommentForPlaylist(Long playlistId, User currentUser, CreateCommentRequest request) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy playlist với ID: " + playlistId));

        PlaylistComment comment = new PlaylistComment(currentUser, request.getContent(), playlist);
        PlaylistComment savedComment = playlistCommentRepository.save(comment);

        return commentMapper.toDto(savedComment);
    }

    public PagedResponse<CommentDto> getCommentsForSong(Long songId, Pageable pageable) {
        if (!songRepository.existsById(songId)) {
            throw new ResourceNotFoundException("Không tìm thấy bài hát với ID: " + songId);
        }
        Page<SongComment> commentPage = songCommentRepository.findBySongIdOrderByCreatedAtDesc(songId, pageable);
        return createPagedResponse(commentPage);
    }

    public PagedResponse<CommentDto> getCommentsForPlaylist(Long playlistId, Pageable pageable) {
        if (!playlistRepository.existsById(playlistId)) {
            throw new ResourceNotFoundException("Không tìm thấy playlist với ID: " + playlistId);
        }
        Page<PlaylistComment> commentPage = playlistCommentRepository.findByPlaylistIdOrderByCreatedAtDesc(playlistId, pageable);
        return createPagedResponse(commentPage);
    }

    @Transactional
    public void deleteSongComment(Long commentId, User currentUser) {
        SongComment comment = songCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bình luận với ID: " + commentId));

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = comment.getUser().getId().equals(currentUser.getId());

        if (!isOwner && !isAdmin) {
            throw new UnauthorizedException("Bạn không có quyền xóa bình luận này.");
        }
        songCommentRepository.delete(comment);
    }

    @Transactional
    public void deletePlaylistComment(Long commentId, User currentUser) {
        PlaylistComment comment = playlistCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bình luận với ID: " + commentId));

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = comment.getUser().getId().equals(currentUser.getId());

        if (!isOwner && !isAdmin) {
            throw new UnauthorizedException("Bạn không có quyền xóa bình luận này.");
        }
        playlistCommentRepository.delete(comment);
    }

    private <T extends BaseComment> PagedResponse<CommentDto> createPagedResponse(Page<T> commentPage) {
        List<CommentDto> commentDtos = commentPage.getContent().stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
        PageInfo pageInfo = new PageInfo(commentPage);
        return new PagedResponse<>(commentDtos, pageInfo);
    }
}