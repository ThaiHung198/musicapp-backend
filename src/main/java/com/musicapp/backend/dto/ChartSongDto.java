package com.musicapp.backend.dto;

import com.musicapp.backend.dto.song.SongDto;
import lombok.Data;

@Data
public class ChartSongDto {
    private int rank;
    private Integer previousRank;
    private SongDto song;
}