package com.musicapp.backend.dto.creator;

import lombok.Data;
import java.util.List;

@Data
public class CreatorViewDto {
    private Long id;
    private String displayName;
    private String email;
    private String phoneNumber;
    private Long approvedSongCount;
}