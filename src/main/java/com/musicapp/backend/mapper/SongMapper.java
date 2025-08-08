package com.musicapp.backend.mapper;

import com.musicapp.backend.dto.song.SongDto;
import com.musicapp.backend.entity.Like;
import com.musicapp.backend.entity.Song;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.repository.CommentRepository;
import com.musicapp.backend.repository.LikeRepository;
import com.musicapp.backend.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SongMapper {

    private final SingerMapper singerMapper;
    private final TagMapper tagMapper;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final SubscriptionService subscriptionService;

    public SongDto toDto(Song song, User currentUser) {
        if (song == null) return null;

        boolean canAccess = true;
        if (song.getIsPremium()) {
            canAccess = (currentUser != null && subscriptionService.hasActivePremiumSubscription(currentUser.getId()));
        }

        return SongDto.builder()
                .id(song.getId())
                .title(song.getTitle())
                .description(song.getDescription())
                .filePath(song.getFilePath())
                .thumbnailPath(song.getThumbnailPath())
                .listenCount(song.getListenCount())
                .status(song.getStatus().name())
                .createdAt(song.getCreatedAt())
                .creatorId(song.getCreator().getId())
                .creatorName(song.getCreator().getDisplayName())
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
                .commentCount(commentRepository.countByCommentableIdAndCommentableType(song.getId(), com.musicapp.backend.entity.Comment.CommentableType.SONG))
                .isLikedByCurrentUser(currentUser != null ?
                        likeRepository.existsByUserIdAndLikeableIdAndLikeableType(
                                currentUser.getId(), song.getId(), Like.LikeableType.SONG) : false)
                .build();
    }

    public SongDto toDtoBasic(Song song) {
        if (song == null) return null;

        return SongDto.builder()
                .id(song.getId())
                .title(song.getTitle())
                .description(song.getDescription())
                .filePath(song.getFilePath())
                .thumbnailPath(song.getThumbnailPath())
                .listenCount(song.getListenCount())
                .status(song.getStatus().name())
                .createdAt(song.getCreatedAt())
                .creatorId(song.getCreator().getId())
                .creatorName(song.getCreator().getDisplayName())
                .build();
    }
}