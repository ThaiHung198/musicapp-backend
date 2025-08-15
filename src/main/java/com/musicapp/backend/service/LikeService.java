package com.musicapp.backend.service;

import com.musicapp.backend.entity.*;
import com.musicapp.backend.exception.BadRequestException;
import com.musicapp.backend.exception.ResourceNotFoundException;
import com.musicapp.backend.repository.LikeRepository;
import com.musicapp.backend.repository.NotificationRepository;
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
    private final NotificationRepository notificationRepository;

    @Transactional
    public boolean toggleSongLike(Long songId, User user) {
        // Sử dụng findById thay vì findByIdAndStatus để lấy được thông tin creator
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found with id: " + songId));

        if (song.getStatus() != Song.SongStatus.APPROVED) {
            throw new BadRequestException("You can only like approved songs.");
        }

        if (likeRepository.existsByUserIdAndLikeableIdAndLikeableType(user.getId(), songId, Like.LikeableType.SONG)) {
            likeRepository.deleteByUserIdAndLikeableIdAndLikeableType(user.getId(), songId, Like.LikeableType.SONG);
            return false;
        } else {
            Like like = Like.builder()
                    .user(user)
                    .likeableId(songId)
                    .likeableType(Like.LikeableType.SONG)
                    .build();
            likeRepository.save(like);

            // --- LOGIC TẠO THÔNG BÁO ---
            User creator = song.getCreator();
            // Chỉ gửi thông báo nếu người like không phải là creator
            if (creator != null && !creator.getId().equals(user.getId())) {
                Notification notification = Notification.builder()
                        .recipient(creator)
                        .actor(user)
                        .type(Notification.NotificationType.SONG_LIKE)
                        .message(user.getDisplayName() + " đã thích bài hát của bạn: " + song.getTitle())
                        .link("/song/" + song.getId()) // Link đến trang chi tiết bài hát
                        .build();
                notificationRepository.save(notification);
            }

            return true;
        }
    }

    @Transactional
    public boolean togglePlaylistLike(Long playlistId, User user) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found with id: " + playlistId));

        if (likeRepository.existsByUserIdAndLikeableIdAndLikeableType(user.getId(), playlistId, Like.LikeableType.PLAYLIST)) {
            likeRepository.deleteByUserIdAndLikeableIdAndLikeableType(user.getId(), playlistId, Like.LikeableType.PLAYLIST);
            return false;
        } else {
            Like like = Like.builder()
                    .user(user)
                    .likeableId(playlistId)
                    .likeableType(Like.LikeableType.PLAYLIST)
                    .build();
            likeRepository.save(like);

            // --- LOGIC TẠO THÔNG BÁO ---
            User creator = playlist.getCreator();
            if (creator != null && !creator.getId().equals(user.getId())) {
                Notification notification = Notification.builder()
                        .recipient(creator)
                        .actor(user)
                        .type(Notification.NotificationType.PLAYLIST_LIKE)
                        .message(user.getDisplayName() + " đã thích playlist của bạn: " + playlist.getName())
                        .link("/playlist/" + playlist.getId()) // Link đến trang chi tiết playlist
                        .build();
                notificationRepository.save(notification);
            }
            // -------------------------

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
