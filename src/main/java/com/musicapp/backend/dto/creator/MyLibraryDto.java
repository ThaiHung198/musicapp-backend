package com.musicapp.backend.dto.creator;

import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.song.SongDto;
import com.musicapp.backend.dto.submission.SubmissionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyLibraryDto {
    private PagedResponse<SubmissionDto> submissions;
    private PagedResponse<SongDto> approvedSongs;
}