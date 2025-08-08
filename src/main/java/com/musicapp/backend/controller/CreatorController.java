package com.musicapp.backend.controller;

import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.dto.creator.MyLibraryDto;
import com.musicapp.backend.entity.SongSubmission;
import com.musicapp.backend.service.CreatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/creators")
@RequiredArgsConstructor
public class CreatorController {

    private final CreatorService creatorService;

    /**
     * Endpoint để creator xem thư viện của mình, bao gồm các yêu cầu và các bài hát đã đăng.
     * Endpoint này hỗ trợ phân trang và lọc/tìm kiếm riêng cho từng danh sách.
     */
    @GetMapping("/my-library")
    @PreAuthorize("hasRole('CREATOR')")
    public ResponseEntity<BaseResponse<MyLibraryDto>> getMyLibrary(
            // Tham số cho danh sách Yêu cầu (Submissions)
            @RequestParam(name = "sub_page", defaultValue = "0") int submissionPage,
            @RequestParam(name = "sub_size", defaultValue = "10") int submissionSize,
            @RequestParam(name = "sub_status", required = false) SongSubmission.SubmissionStatus submissionStatus,

            // Tham số cho danh sách Bài hát đã đăng (Approved Songs)
            @RequestParam(name = "song_page", defaultValue = "0") int songPage,
            @RequestParam(name = "song_size", defaultValue = "10") int songSize,
            @RequestParam(name = "song_keyword", required = false) String songKeyword,

            Authentication authentication
    ) {
        String username = authentication.getName();

        // Tạo Pageable cho từng danh sách
        Pageable submissionPageable = PageRequest.of(submissionPage, submissionSize);
        Pageable songPageable = PageRequest.of(songPage, songSize);

        // Gọi service để lấy dữ liệu
        MyLibraryDto libraryData = creatorService.getMyLibrary(
                username,
                submissionStatus,
                submissionPageable,
                songKeyword,
                songPageable
        );

        return ResponseEntity.ok(BaseResponse.success("Creator library retrieved successfully", libraryData));
    }
}