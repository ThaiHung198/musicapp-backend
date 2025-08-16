package com.musicapp.backend.mapper;

import com.musicapp.backend.dto.creator.CreatorDetailDto;
import com.musicapp.backend.dto.song.SongDto;
import com.musicapp.backend.dto.user.UserProfileDto;
import com.musicapp.backend.dto.user.AdminUserViewDto;
import com.musicapp.backend.entity.Role;
import com.musicapp.backend.entity.Song;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.entity.UserSubscription;
import com.musicapp.backend.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final SongMapper songMapper;
    private final SubscriptionService subscriptionService;

    public UserProfileDto toUserProfileDto(User user) {
        if (user == null) {
            return null;
        }

        Optional<UserSubscription> activeSubscriptionOpt = user.getSubscriptions().stream()
                .filter(sub -> sub.getStatus() == UserSubscription.SubscriptionStatus.ACTIVE)
                .findFirst();

        boolean hasActiveSub = subscriptionService.hasActivePremiumSubscription(user.getId());

        UserProfileDto.UserProfileDtoBuilder builder = UserProfileDto.builder()
                .id(user.getId())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .avatarPath(user.getAvatarPath())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .provider(user.getProvider())
                .createdAt(user.getCreatedAt())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()))
                .hasActiveSubscription(hasActiveSub);

        activeSubscriptionOpt.ifPresent(sub -> {
            builder.subscriptionStatus(sub.getStatus().name());
            builder.subscriptionEndDate(sub.getEndDate().toLocalDate());
        });

        return builder.build();
    }

    public AdminUserViewDto toAdminUserViewDto(User user, boolean isPremium) {
        if (user == null) {
            return null;
        }

        String premiumStatus = isPremium ? "PREMIUM" : "FREE";

        return AdminUserViewDto.builder()
                .id(user.getId())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .premiumStatus(premiumStatus)
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()))
                .build();
    }

    public CreatorDetailDto toCreatorDetailDto(User user, List<Song> songs) {
        if (user == null) {
            return null;
        }

        CreatorDetailDto dto = new CreatorDetailDto();
        dto.setId(user.getId());
        dto.setDisplayName(user.getDisplayName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAvatarPath(user.getAvatarPath());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setGender(user.getGender());
        dto.setCreatedAt(user.getCreatedAt());

        List<SongDto> songDtos = songs.stream()
                .map(song -> songMapper.toDtoBasic(song))
                .collect(Collectors.toList());
        dto.setSongs(songDtos);

        return dto;
    }
}