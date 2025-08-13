package com.musicapp.backend.mapper;

import com.musicapp.backend.dto.tag.TagDto;
import com.musicapp.backend.entity.Tag;
import com.musicapp.backend.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TagMapper {

    private final SongRepository songRepository;

    public TagDto toDto(Tag tag) {
        if (tag == null) return null;

        long songCount = songRepository.countByTagsContains(tag);

        return TagDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .songCount(songCount)
                .build();
    }
}