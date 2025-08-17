package com.musicapp.backend.dto.tag;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagAdminViewDto {
    private Long id;
    private String name;
    private long songCount;
}