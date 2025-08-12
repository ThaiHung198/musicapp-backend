package com.musicapp.backend.dto.playlist;

import lombok.Data;
import java.util.List;

@Data
public class UpdatePlaylistRequest {

    private String name;

    private List<Long> songIds;
}