package com.musicapp.backend.dto.singer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SingerDto {
    private Long id;
    private String name;
    private String avatarPath;
    private Long songCount;
}
