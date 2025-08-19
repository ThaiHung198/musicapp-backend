package com.musicapp.backend.mapper;

import com.musicapp.backend.dto.song.SongDto;
import com.musicapp.backend.entity.Like;
import com.musicapp.backend.entity.Song;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.repository.LikeRepository;
import com.musicapp.backend.repository.SongCommentRepository;
import com.musicapp.backend.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SongMapper {

    private final SingerMapper singerMapper;
    private final TagMapper tagMapper;
    private final LikeRepository likeRepository;
    private final SongCommentRepository songCommentRepository;
    private final SubscriptionService subscriptionService;

    @Transactional(readOnly = true)
    public SongDto toDto(Song song, User currentUser) {
        if (song == null) return null;

        boolean canAccess = true;
        if (song.getIsPremium()) {
            canAccess = (currentUser != null && subscriptionService.hasActivePremiumSubscription(currentUser.getId()));
        }

        Long creatorId = song.getCreator() != null ? song.getCreator().getId() : null;
        String creatorName = song.getCreator() != null ? song.getCreator().getDisplayName() : "Hệ thống";

        return SongDto.builder()
                .id(song.getId())
                .title(song.getTitle())
                .description(song.getDescription())
                .filePath(song.getFilePath())
                .thumbnailPath(song.getThumbnailPath())
                .listenCount(song.getListenCount())
                .status(song.getStatus().name())
                .createdAt(song.getCreatedAt())
                .color(song.getColor())
                .creatorId(creatorId)
                .creatorName(creatorName)
                .isPremium(song.getIsPremium())
                .canAccess(canAccess)
                .singers(song.getSingers() != null ?
                        song.getSingers().stream()
                                .map(singerMapper::toDto)
                                .collect(Collectors.toList()) : null)
                .tags(song.getTags() != null ?
                        song.getTags().stream()
                                .map(tagMapper::toDto)
                                .collect(Collectors.toList()) : null)
                .likeCount(likeRepository.countByLikeableIdAndLikeableType(song.getId(), Like.LikeableType.SONG))
                .commentCount(songCommentRepository.countBySongId(song.getId()))
                .isLikedByCurrentUser(currentUser != null &&
                        likeRepository.existsByUserIdAndLikeableIdAndLikeableType(
                                currentUser.getId(), song.getId(), Like.LikeableType.SONG))
                .build();
    }

    @Transactional(readOnly = true)
    public SongDto toDtoBasic(Song song) {
        if (song == null) return null;

        Long creatorId = song.getCreator() != null ? song.getCreator().getId() : null;
        String creatorName = song.getCreator() != null ? song.getCreator().getDisplayName() : "Hệ thống";

        return SongDto.builder()
                .id(song.getId())
                .title(song.getTitle())
                .description(song.getDescription())
                .filePath(song.getFilePath())
                .thumbnailPath(song.getThumbnailPath())
                .listenCount(song.getListenCount())
                .status(song.getStatus().name())
                .createdAt(song.getCreatedAt())
                .color(song.getColor())
                .creatorId(creatorId)
                .creatorName(creatorName)
                .build();
    }
}