package com.musicapp.backend.dto.playlist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPlaylistManagementDto {
    private List<PlaylistDto> adminPlaylists;
    private List<PlaylistDto> creatorPlaylists;
}