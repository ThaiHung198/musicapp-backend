package com.musicapp.backend.dto.playlist;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class AddSongsToPlaylistRequest {

    @NotEmpty(message = "Danh sách ID bài hát không được để trống.")
    private List<Long> songIds;
}