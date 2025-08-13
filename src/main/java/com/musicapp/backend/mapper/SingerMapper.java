package com.musicapp.backend.mapper;

import com.musicapp.backend.dto.singer.SingerDetailDto;
import com.musicapp.backend.dto.singer.SingerDto;
import com.musicapp.backend.dto.song.SongDto;
import com.musicapp.backend.entity.Singer;
import com.musicapp.backend.repository.SingerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SingerMapper {

    private final SingerRepository singerRepository;

    public SingerDto toDto(Singer singer) {
        if (singer == null) return null;

        return SingerDto.builder()
                .id(singer.getId())
                .name(singer.getName())
                .email(singer.getEmail())
                .avatarPath(singer.getAvatarPath())
                .creatorId(singer.getCreator() != null ? singer.getCreator().getId() : null)
                .creatorName(singer.getCreator() != null ? singer.getCreator().getDisplayName() : null)
                .songCount(singerRepository.countSongsBySingerId(singer.getId()))
                .status(singer.getStatus().name())
                .build();
    }

    public SingerDto toDtoWithoutSongCount(Singer singer) {
        if (singer == null) return null;

        return SingerDto.builder()
                .id(singer.getId())
                .name(singer.getName())
                .email(singer.getEmail())
                .avatarPath(singer.getAvatarPath())
                .creatorId(singer.getCreator() != null ? singer.getCreator().getId() : null)
                .creatorName(singer.getCreator() != null ? singer.getCreator().getDisplayName() : null)
                .status(singer.getStatus().name())
                .build();
    }

    public SingerDetailDto toDetailDto(Singer singer, List<SongDto> songs) {
        if (singer == null) return null;
        return SingerDetailDto.builder()
                .id(singer.getId())
                .name(singer.getName())
                .avatarPath(singer.getAvatarPath())
                .songCount((long) songs.size())
                .songs(songs)
                .build();
    }
}