package com.musicapp.backend.dto.creator;

import com.musicapp.backend.dto.song.SongDto;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreatorDetailDto {
    private Long id;
    private String displayName;
    private String email;
    private String phoneNumber;
    private String avatarPath;
    private LocalDate dateOfBirth;
    private String gender;
    private LocalDateTime createdAt;
    private List<SongDto> songs;
}