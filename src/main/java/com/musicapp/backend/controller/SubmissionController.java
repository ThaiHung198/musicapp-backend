package com.musicapp.backend.controller;

import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.submission.CreateSubmissionRequest;
import com.musicapp.backend.dto.submission.ReviewSubmissionRequest;
import com.musicapp.backend.dto.submission.SubmissionDto;
import com.musicapp.backend.entity.SongSubmission.SubmissionStatus;
import com.musicapp.backend.service.SubmissionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/submissions")
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @PreAuthorize("hasRole('CREATOR')")
    public ResponseEntity<BaseResponse<SubmissionDto>> createSubmission(
            @RequestPart("submissionRequest") @Valid CreateSubmissionRequest request,
            @RequestPart("audioFile") MultipartFile audioFile,
            @RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
            Authentication authentication) {

        String username = authentication.getName();
        SubmissionDto submission = submissionService.createSubmission(request, audioFile, thumbnailFile, username);
        return ResponseEntity.ok(BaseResponse.success("Submission created successfully", submission));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<SubmissionDto>> getMySubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) SubmissionStatus status,
            Authentication authentication) {
        String username = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<SubmissionDto> submissions = submissionService.getSubmissionsByUser(username, status, pageable);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<SubmissionDto>> getSubmission(
            @PathVariable Long id,
            Authentication authentication) {

        String username = authentication.getName();
        SubmissionDto submission = submissionService.getSubmissionById(id, username);
        return ResponseEntity.ok(BaseResponse.success("Submission retrieved successfully", submission));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<SubmissionDto>> updateSubmission(
            @PathVariable Long id,
            @Valid @RequestBody CreateSubmissionRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        SubmissionDto submission = submissionService.updateSubmission(id, request, username);
        return ResponseEntity.ok(BaseResponse.success("Submission updated successfully", submission));
    }

    @DeleteMapping("/{id}/withdraw")
    @PreAuthorize("hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deleteSubmission(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        submissionService.deleteSubmission(id, username);
        return ResponseEntity.ok(BaseResponse.success("Submission withdrawn successfully", null));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<SubmissionDto>> getPendingSubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<SubmissionDto> submissions = submissionService.getSubmissionsByStatus(SubmissionStatus.PENDING, pageable);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<SubmissionDto>> getAllSubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) SubmissionStatus status,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<SubmissionDto> submissions = submissionService.searchSubmissions(search, status, pageable);
        return ResponseEntity.ok(submissions);
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<SubmissionDto>> reviewSubmission(
            @PathVariable Long id,
            @Valid @RequestBody ReviewSubmissionRequest request,
            Authentication authentication) {
        String reviewerUsername = authentication.getName();
        SubmissionDto submission = submissionService.reviewSubmission(id, request, reviewerUsername);
        return ResponseEntity.ok(BaseResponse.success("Submission reviewed successfully", submission));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<SubmissionDto>> approveSubmission(
            @PathVariable Long id,
            Authentication authentication) {
        String reviewerUsername = authentication.getName();
        SubmissionDto submission = submissionService.approveSubmission(id, reviewerUsername);
        return ResponseEntity.ok(BaseResponse.success("Submission approved successfully", submission));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<SubmissionDto>> rejectSubmission(
            @PathVariable Long id,
            @RequestParam String reason,
            Authentication authentication) {
        String reviewerUsername = authentication.getName();
        SubmissionDto submission = submissionService.rejectSubmission(id, reason, reviewerUsername);
        return ResponseEntity.ok(BaseResponse.success("Submission rejected successfully", submission));
    }

    @GetMapping("/stats/user")
    @PreAuthorize("hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Object>> getUserSubmissionStats(Authentication authentication) {
        String username = authentication.getName();
        Object stats = submissionService.getUserSubmissionStats(username);
        return ResponseEntity.ok(BaseResponse.success("User submission statistics retrieved", stats));
    }

    @GetMapping("/stats/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Object>> getAdminSubmissionStats() {
        Object stats = submissionService.getAdminSubmissionStats();
        return ResponseEntity.ok(BaseResponse.success("Admin submission statistics retrieved", stats));
    }
}