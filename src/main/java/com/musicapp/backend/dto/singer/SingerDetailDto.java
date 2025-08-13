package com.musicapp.backend.dto.singer;

import com.musicapp.backend.dto.song.SongDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SingerDetailDto {
    private Long id;
    private String name;
    private String avatarPath;
    private Long songCount;
    private List<SongDto> songs;
}