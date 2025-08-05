package com.musicapp.backend.mapper;

import com.musicapp.backend.dto.singer.SingerDto;
import com.musicapp.backend.entity.Singer;
import com.musicapp.backend.repository.SingerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SingerMapper {
    
    private final SingerRepository singerRepository;
    
    public SingerDto toDto(Singer singer) {
        if (singer == null) return null;
        
        return SingerDto.builder()
                .id(singer.getId())
                .name(singer.getName())
                .avatarPath(singer.getAvatarPath())
                .songCount(singerRepository.countSongsBySingerId(singer.getId()))
                .build();
    }
    
    public SingerDto toDtoWithoutSongCount(Singer singer) {
        if (singer == null) return null;
        
        return SingerDto.builder()
                .id(singer.getId())
                .name(singer.getName())
                .avatarPath(singer.getAvatarPath())
                .build();
    }
}
