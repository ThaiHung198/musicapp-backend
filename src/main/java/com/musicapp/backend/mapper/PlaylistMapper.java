package com.musicapp.backend.mapper;

import com.musicapp.backend.dto.playlist.PlaylistDetailDto;
import com.musicapp.backend.dto.playlist.PlaylistDto;
import com.musicapp.backend.entity.Like;
import com.musicapp.backend.entity.Playlist;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PlaylistMapper {

    private final SongMapper songMapper;
    private final LikeRepository likeRepository;

    public PlaylistDto toDto(Playlist playlist, User currentUser) {
        if (playlist == null) {
            return null;
        }

        return PlaylistDto.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .thumbnailPath(playlist.getThumbnailPath())
                .visibility(playlist.getVisibility().name())
                .createdAt(playlist.getCreatedAt())
                .creatorId(playlist.getCreator() != null ? playlist.getCreator().getId() : null)
                .creatorName(playlist.getCreator() != null ? playlist.getCreator().getDisplayName() : "Hệ thống")
                .songCount(playlist.getSongs() != null ? playlist.getSongs().size() : 0)
                .likeCount(likeRepository.countByLikeableIdAndLikeableType(playlist.getId(), Like.LikeableType.PLAYLIST))
                .isLikedByCurrentUser(currentUser != null && likeRepository.existsByUserIdAndLikeableIdAndLikeableType(
                        currentUser.getId(), playlist.getId(), Like.LikeableType.PLAYLIST))
                .build();
    }

    public PlaylistDetailDto toDetailDto(Playlist playlist, User currentUser) {
        if (playlist == null) {
            return null;
        }

        return PlaylistDetailDto.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .thumbnailPath(playlist.getThumbnailPath())
                .visibility(playlist.getVisibility().name())
                .createdAt(playlist.getCreatedAt())
                .creatorId(playlist.getCreator() != null ? playlist.getCreator().getId() : null)
                .creatorName(playlist.getCreator() != null ? playlist.getCreator().getDisplayName() : "Hệ thống")
                .songCount(playlist.getSongs() != null ? playlist.getSongs().size() : 0)
                .songs(playlist.getSongs() != null ? playlist.getSongs().stream()
                        .map(song -> songMapper.toDto(song, currentUser))
                        .collect(Collectors.toList()) : null)
                .likeCount(likeRepository.countByLikeableIdAndLikeableType(playlist.getId(), Like.LikeableType.PLAYLIST))
                .isLikedByCurrentUser(currentUser != null && likeRepository.existsByUserIdAndLikeableIdAndLikeableType(
                        currentUser.getId(), playlist.getId(), Like.LikeableType.PLAYLIST))
                .build();
    }
}