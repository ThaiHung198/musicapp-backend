package com.musicapp.backend.service;

import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.creator.MyLibraryDto;
import com.musicapp.backend.dto.song.SongDto;
import com.musicapp.backend.dto.submission.SubmissionDto;
import com.musicapp.backend.entity.SongSubmission;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreatorService {

    private final SubmissionService submissionService;
    private final SongService songService;

    public MyLibraryDto getMyLibrary(
            String username,
            SongSubmission.SubmissionStatus submissionStatus,
            Pageable submissionPageable,
            String songKeyword,
            Pageable songPageable
    ) {
        // 1. Lấy danh sách các yêu cầu đã gửi (submissions)
        PagedResponse<SubmissionDto> submissions = submissionService.getSubmissionsByUser(
                username,
                null, // Thêm tham số keyword còn thiếu
                submissionStatus,
                submissionPageable
        );

        // 2. Lấy danh sách các bài hát đã được đăng (approved songs)
        PagedResponse<SongDto> approvedSongs = songService.getMyLibrary(
                username,
                songKeyword,
                songPageable
        );

        // 3. Xây dựng và trả về DTO tổng hợp
        return MyLibraryDto.builder()
                .submissions(submissions)
                .approvedSongs(approvedSongs)
                .build();
    }
}