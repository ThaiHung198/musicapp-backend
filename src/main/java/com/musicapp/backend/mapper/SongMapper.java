package com.musicapp.backend.mapper;

import com.musicapp.backend.dto.song.SongDto;
import com.musicapp.backend.entity.Like;
import com.musicapp.backend.entity.Song;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.entity.UserSubscription;
import com.musicapp.backend.repository.CommentRepository;
import com.musicapp.backend.repository.LikeRepository;
import com.musicapp.backend.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SongMapper {

    private final SingerMapper singerMapper;
    private final TagMapper tagMapper;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;

    public SongDto toDto(Song song, User currentUser) {
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
                .isPremium(song.getIsPremium())
                .premiumPrice(song.getPremiumPrice())
                .canAccess(canUserAccessSong(song, currentUser))
                // The concept of individual purchase is removed.
                // If you want to keep this field in DTO, it should reflect subscription status.
                // For simplicity in a base project, we can consider access via subscription as "purchased".
                .isPurchased(canUserAccessSong(song, currentUser) && song.getIsPremium())
                .singers(song.getSingers() != null ?
                        song.getSingers().stream()
                                .map(singerMapper::toDtoWithoutSongCount)
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

    private boolean canUserAccessSong(Song song, User currentUser) {
        // Free songs are accessible to everyone
        if (!song.getIsPremium()) {
            return true;
        }

        // For premium songs, only logged-in users with a subscription can access
        if (currentUser == null) {
            return false;
        }

        // Check if the user has an active subscription that covers premium content
        return userSubscriptionRepository.findUserActiveSubscriptions(currentUser, LocalDateTime.now())
                .stream()
                .anyMatch(subscription -> subscription.getSubscriptionType().ordinal() >= UserSubscription.SubscriptionType.PREMIUM.ordinal());
    }
}