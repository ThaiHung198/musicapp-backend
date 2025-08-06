package com.musicapp.backend.service;

import com.musicapp.backend.dto.submission.CreateSubmissionRequest;
import com.musicapp.backend.dto.submission.ReviewSubmissionRequest;
import com.musicapp.backend.dto.submission.SubmissionDto;
import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.entity.*;
import com.musicapp.backend.exception.BadRequestException;
import com.musicapp.backend.exception.ResourceNotFoundException;
import com.musicapp.backend.exception.UnauthorizedException;
import com.musicapp.backend.mapper.SubmissionMapper;
import com.musicapp.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubmissionService {

    private final SongSubmissionRepository submissionRepository;
    private final SingerRepository singerRepository;
    private final TagRepository tagRepository;
    private final SongRepository songRepository;
    private final UserRepository userRepository;
    private final SubmissionSingersRepository submissionSingersRepository;
    private final SubmissionTagsRepository submissionTagsRepository;
    private final SubmissionMapper submissionMapper;

    // Creator methods
    @Transactional
    public SubmissionDto createSubmission(CreateSubmissionRequest request, User creator) {
        // Validate singers exist
        Set<Singer> singers = new HashSet<>();
        for (Long singerId : request.getSingerIds()) {
            Singer singer = singerRepository.findById(singerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Singer not found with id: " + singerId));
            singers.add(singer);
        }

        // Validate tags exist (if provided)
        Set<Tag> tags = new HashSet<>();
        if (request.getTagIds() != null) {
            for (Long tagId : request.getTagIds()) {
                Tag tag = tagRepository.findById(tagId)
                        .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + tagId));
                tags.add(tag);
            }
        }

        // Validate premium price if premium song
        if (request.getIsPremium() && (request.getPremiumPrice() == null || request.getPremiumPrice().compareTo(java.math.BigDecimal.ZERO) <= 0)) {
            throw new BadRequestException("Premium price is required for premium songs and must be greater than 0");
        }

        SongSubmission submission = SongSubmission.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .filePath(request.getFilePath())
                .thumbnailPath(request.getThumbnailPath())
                .isPremium(request.getIsPremium())
                .premiumPrice(request.getPremiumPrice())
                .creator(creator)
                .build();

        SongSubmission savedSubmission = submissionRepository.save(submission);

        // Create singer associations
        for (Singer singer : singers) {
            SubmissionSingers submissionSinger = SubmissionSingers.builder()
                    .submission(savedSubmission)
                    .singer(singer)
                    .build();
            submissionSingersRepository.save(submissionSinger);
        }

        // Create tag associations
        for (Tag tag : tags) {
            SubmissionTags submissionTag = SubmissionTags.builder()
                    .submission(savedSubmission)
                    .tag(tag)
                    .build();
            submissionTagsRepository.save(submissionTag);
        }

        return submissionMapper.toDto(savedSubmission, creator);
    }

    public Page<SubmissionDto> getMySubmissions(User creator, Pageable pageable) {
        return submissionRepository.findByCreatorIdOrderBySubmissionDateDesc(creator.getId(), pageable)
                .map(submission -> submissionMapper.toDto(submission, creator));
    }

    public Page<SubmissionDto> getMySubmissionsByStatus(User creator, String status, Pageable pageable) {
        SongSubmission.SubmissionStatus submissionStatus;
        try {
            submissionStatus = SongSubmission.SubmissionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid submission status: " + status);
        }

        return submissionRepository.findByCreatorIdAndStatusOrderBySubmissionDateDesc(creator.getId(), submissionStatus, pageable)
                .map(submission -> submissionMapper.toDto(submission, creator));
    }

    public SubmissionDto getSubmissionById(Long id, User user) {
        SongSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + id));

        // Check permission
        if (!submission.getCreator().getId().equals(user.getId()) && !hasAdminRole(user)) {
            throw new UnauthorizedException("You don't have permission to access this submission");
        }

        return submissionMapper.toDto(submission, user);
    }

    @Transactional
    public void deleteSubmission(Long id, User user) {
        SongSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + id));

        // Check permission - only creator can delete pending submissions, admin can delete any
        if (!submission.getCreator().getId().equals(user.getId()) && !hasAdminRole(user)) {
            throw new UnauthorizedException("You don't have permission to delete this submission");
        }

        // Can only delete pending submissions
        if (submission.getStatus() != SongSubmission.SubmissionStatus.PENDING) {
            throw new BadRequestException("Can only delete pending submissions");
        }

        submissionRepository.delete(submission);
    }

    // Admin methods
    public Page<SubmissionDto> getAllSubmissions(Pageable pageable) {
        return submissionRepository.findAll(pageable)
                .map(submission -> submissionMapper.toDto(submission, null));
    }

    public Page<SubmissionDto> getSubmissionsByStatus(String status, Pageable pageable) {
        SongSubmission.SubmissionStatus submissionStatus;
        try {
            submissionStatus = SongSubmission.SubmissionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid submission status: " + status);
        }

        return submissionRepository.findByStatusOrderBySubmissionDateDesc(submissionStatus, pageable)
                .map(submission -> submissionMapper.toDto(submission, null));
    }

    public Page<SubmissionDto> getPendingSubmissions(Pageable pageable) {
        return submissionRepository.findPendingSubmissions(pageable)
                .map(submission -> submissionMapper.toDto(submission, null));
    }

    @Transactional
    public SubmissionDto reviewSubmission(Long id, ReviewSubmissionRequest request, User reviewer) {
        SongSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + id));

        if (submission.getStatus() != SongSubmission.SubmissionStatus.PENDING) {
            throw new BadRequestException("Can only review pending submissions");
        }

        submission.setReviewer(reviewer);
        submission.setReviewDate(LocalDateTime.now());

        if ("APPROVE".equalsIgnoreCase(request.getAction())) {
            submission.setStatus(SongSubmission.SubmissionStatus.APPROVED);

            // Create approved song
            Song approvedSong = Song.builder()
                    .title(submission.getTitle())
                    .description(submission.getDescription())
                    .filePath(submission.getFilePath())
                    .thumbnailPath(submission.getThumbnailPath())
                    .isPremium(submission.getIsPremium())
                    .premiumPrice(submission.getPremiumPrice())
                    .creator(submission.getCreator())
                    .status(Song.SongStatus.APPROVED)
                    .submission(submission)
                    .build();

            Song savedSong = songRepository.save(approvedSong);
            submission.setApprovedSong(savedSong);

            // Copy singers and tags to approved song
            // This would require additional logic to copy relationships

        } else if ("REJECT".equalsIgnoreCase(request.getAction())) {
            submission.setStatus(SongSubmission.SubmissionStatus.REJECTED);
            submission.setRejectionReason(request.getRejectionReason());
        } else {
            throw new BadRequestException("Invalid review action. Must be APPROVE or REJECT");
        }

        SongSubmission updatedSubmission = submissionRepository.save(submission);
        return submissionMapper.toDto(updatedSubmission, reviewer);
    }

    // Search and statistics
    public Page<SubmissionDto> searchSubmissions(String keyword, String status, Pageable pageable) {
        SongSubmission.SubmissionStatus submissionStatus;
        try {
            submissionStatus = SongSubmission.SubmissionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid submission status: " + status);
        }

        return submissionRepository.searchByTitleAndStatus(keyword, submissionStatus, pageable)
                .map(submission -> submissionMapper.toDto(submission, null));
    }

    public long getSubmissionCountByStatus(String status) {
        SongSubmission.SubmissionStatus submissionStatus;
        try {
            submissionStatus = SongSubmission.SubmissionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid submission status: " + status);
        }

        return submissionRepository.countByStatus(submissionStatus);
    }

    private boolean hasAdminRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
    }

    // Username-based methods for controller compatibility
    @Transactional
    public SubmissionDto createSubmission(CreateSubmissionRequest request, String username) {
        User creator = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));
        return createSubmission(request, creator);
    }

    public PagedResponse<SubmissionDto> getSubmissionsByUser(String username, SongSubmission.SubmissionStatus status, Pageable pageable) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));

        Page<SubmissionDto> submissions;
        if (status != null) {
            submissions = getMySubmissionsByStatus(user, status.name(), pageable);
        } else {
            submissions = getMySubmissions(user, pageable);
        }

        return createPagedResponse(submissions);
    }

    public SubmissionDto getSubmissionById(Long id, String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));
        return getSubmissionById(id, user);
    }

    @Transactional
    public SubmissionDto updateSubmission(Long id, CreateSubmissionRequest request, String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));

        SongSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + id));

        // Check permission
        if (!submission.getCreator().getId().equals(user.getId()) && !hasAdminRole(user)) {
            throw new UnauthorizedException("You don't have permission to update this submission");
        }

        // Can only update pending submissions
        if (submission.getStatus() != SongSubmission.SubmissionStatus.PENDING) {
            throw new BadRequestException("Can only update pending submissions");
        }

        // Update submission fields
        submission.setTitle(request.getTitle());
        submission.setDescription(request.getDescription());
        submission.setFilePath(request.getFilePath());
        submission.setThumbnailPath(request.getThumbnailPath());
        submission.setIsPremium(request.getIsPremium());
        submission.setPremiumPrice(request.getPremiumPrice());

        SongSubmission updatedSubmission = submissionRepository.save(submission);
        return submissionMapper.toDto(updatedSubmission, user);
    }

    @Transactional
    public void deleteSubmission(Long id, String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));
        deleteSubmission(id, user);
    }

    public PagedResponse<SubmissionDto> getSubmissionsByStatus(SongSubmission.SubmissionStatus status, Pageable pageable) {
        Page<SubmissionDto> submissions = getSubmissionsByStatus(status.name(), pageable);
        return createPagedResponse(submissions);
    }

    public PagedResponse<SubmissionDto> searchSubmissions(String search, SongSubmission.SubmissionStatus status, Pageable pageable) {
        Page<SubmissionDto> submissions;
        if (search != null && !search.trim().isEmpty()) {
            submissions = searchSubmissions(search.trim(), status.name(), pageable);
        } else {
            submissions = getSubmissionsByStatus(status.name(), pageable);
        }
        return createPagedResponse(submissions);
    }

    public SubmissionDto reviewSubmission(Long id, ReviewSubmissionRequest request, String reviewerUsername) {
        User reviewer = userRepository.findByEmail(reviewerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found with email: " + reviewerUsername));
        return reviewSubmission(id, request, reviewer);
    }

    @Transactional
    public SubmissionDto approveSubmission(Long id, String reviewerUsername) {
        User reviewer = userRepository.findByEmail(reviewerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found with email: " + reviewerUsername));

        ReviewSubmissionRequest request = ReviewSubmissionRequest.builder()
                .action("APPROVE")
                .build();

        return reviewSubmission(id, request, reviewer);
    }

    @Transactional
    public SubmissionDto rejectSubmission(Long id, String reason, String reviewerUsername) {
        User reviewer = userRepository.findByEmail(reviewerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found with email: " + reviewerUsername));

        ReviewSubmissionRequest request = ReviewSubmissionRequest.builder()
                .action("REJECT")
                .rejectionReason(reason)
                .build();

        return reviewSubmission(id, request, reviewer);
    }

    public Object getUserSubmissionStats(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));

        return Map.of(
                "totalSubmissions", submissionRepository.countByCreatorId(user.getId()),
                "pendingSubmissions", submissionRepository.countByCreatorIdAndStatus(user.getId(), SongSubmission.SubmissionStatus.PENDING),
                "approvedSubmissions", submissionRepository.countByCreatorIdAndStatus(user.getId(), SongSubmission.SubmissionStatus.APPROVED),
                "rejectedSubmissions", submissionRepository.countByCreatorIdAndStatus(user.getId(), SongSubmission.SubmissionStatus.REJECTED)
        );
    }

    public Object getAdminSubmissionStats() {
        return Map.of(
                "totalSubmissions", submissionRepository.count(),
                "pendingSubmissions", submissionRepository.countByStatus(SongSubmission.SubmissionStatus.PENDING),
                "approvedSubmissions", submissionRepository.countByStatus(SongSubmission.SubmissionStatus.APPROVED),
                "rejectedSubmissions", submissionRepository.countByStatus(SongSubmission.SubmissionStatus.REJECTED),
                "reviewingSubmissions", submissionRepository.countByStatus(SongSubmission.SubmissionStatus.REVIEWING)
        );
    }

    // Helper method to create paged response
    private <T> PagedResponse<T> createPagedResponse(Page<T> page) {
        return PagedResponse.of(page.getContent(), page);
    }
}