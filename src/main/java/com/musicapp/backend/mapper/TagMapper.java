package com.musicapp.backend.mapper;

import com.musicapp.backend.dto.tag.TagDto;
import com.musicapp.backend.entity.Tag;
import com.musicapp.backend.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TagMapper {

    private final TagRepository tagRepository;
    
    public TagDto toDto(Tag tag) {
        if (tag == null) return null;
        
        return TagDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                // Lấy số lượng bài hát
                .songCount(tagRepository.countSongsByTagId(tag.getId()))
                .build();
    }
}
