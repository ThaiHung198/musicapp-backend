package com.musicapp.backend.mapper;

import com.musicapp.backend.dto.tag.TagDto;
import com.musicapp.backend.entity.Tag;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {
    
    public TagDto toDto(Tag tag) {
        if (tag == null) return null;
        
        return TagDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .build();
    }
}
