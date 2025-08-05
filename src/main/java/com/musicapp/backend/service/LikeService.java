package com.musicapp.backend.service;

import com.musicapp.backend.entity.Like;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.exception.BadRequestException;
import com.musicapp.backend.exception.ResourceNotFoundException;
import com.musicapp.backend.repository.LikeRepository;
import com.musicapp.backend.repository.PlaylistRepository;
import com.musicapp.backend.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeService {
    
    private final LikeRepository likeRepository;
    private final SongRepository songRepository;
    private final PlaylistRepository playlistRepository;
    
    @Transactional
    public boolean toggleSongLike(Long songId, User user) {
        // Verify song exists and is approved
        if (!songRepository.findByIdAndStatus(songId, com.musicapp.backend.entity.Song.SongStatus.APPROVED).isPresent()) {
            throw new ResourceNotFoundException("Song not found or not approved with id: " + songId);
        }
        
        // Check if already liked
        if (likeRepository.existsByUserIdAndLikeableIdAndLikeableType(
                user.getId(), songId, Like.LikeableType.SONG)) {
            // Unlike
            likeRepository.deleteByUserIdAndLikeableIdAndLikeableType(
                    user.getId(), songId, Like.LikeableType.SONG);
            return false;
        } else {
            // Like
            Like like = Like.builder()
                    .user(user)
                    .likeableId(songId)
                    .likeableType(Like.LikeableType.SONG)
                    .build();
            likeRepository.save(like);
            return true;
        }
    }
    
    @Transactional
    public boolean togglePlaylistLike(Long playlistId, User user) {
        // Verify playlist exists
        if (!playlistRepository.existsById(playlistId)) {
            throw new ResourceNotFoundException("Playlist not found with id: " + playlistId);
        }
        
        // Check if already liked
        if (likeRepository.existsByUserIdAndLikeableIdAndLikeableType(
                user.getId(), playlistId, Like.LikeableType.PLAYLIST)) {
            // Unlike
            likeRepository.deleteByUserIdAndLikeableIdAndLikeableType(
                    user.getId(), playlistId, Like.LikeableType.PLAYLIST);
            return false;
        } else {
            // Like
            Like like = Like.builder()
                    .user(user)
                    .likeableId(playlistId)
                    .likeableType(Like.LikeableType.PLAYLIST)
                    .build();
            likeRepository.save(like);
            return true;
        }
    }
    
    public boolean isSongLikedByUser(Long songId, Long userId) {
        return likeRepository.existsByUserIdAndLikeableIdAndLikeableType(
                userId, songId, Like.LikeableType.SONG);
    }
    
    public boolean isPlaylistLikedByUser(Long playlistId, Long userId) {
        return likeRepository.existsByUserIdAndLikeableIdAndLikeableType(
                userId, playlistId, Like.LikeableType.PLAYLIST);
    }
    
    public long getSongLikeCount(Long songId) {
        return likeRepository.countByLikeableIdAndLikeableType(songId, Like.LikeableType.SONG);
    }
    
    public long getPlaylistLikeCount(Long playlistId) {
        return likeRepository.countByLikeableIdAndLikeableType(playlistId, Like.LikeableType.PLAYLIST);
    }
    
    public long getUserLikeCount(Long userId) {
        return likeRepository.countByUserId(userId);
    }
}
