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
import com.musicapp.backend.dto.submission.CreateSubmissionRequest.NewSingerInfo;
import com.musicapp.backend.entity.Singer.SingerStatus;
import com.musicapp.backend.exception.ResourceAlreadyExistsException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubmissionService {

    private final SongSubmissionRepository submissionRepository;
    private final SingerRepository singerRepository;
    private final TagRepository tagRepository;
    private final SongRepository songRepository;
    private final UserRepository userRepository;
    private final SubmissionSingersRepository submissionSingersRepository; // Vẫn giữ lại để dùng ở nơi khác nếu cần
    private final SubmissionTagsRepository submissionTagsRepository; // Vẫn giữ lại
    private final SubmissionMapper submissionMapper;

    @Transactional
    public SubmissionDto createSubmission(CreateSubmissionRequest request, User creator) {
        // 1. Validate request đầu vào
        if ((request.getExistingSingerIds() == null || request.getExistingSingerIds().isEmpty()) &&
                (request.getNewSingers() == null || request.getNewSingers().isEmpty())) {
            throw new BadRequestException("At least one existing or new singer is required.");
        }

        Set<Singer> allSingersForSubmission = new HashSet<>();

        // 2. Xử lý và tạo ca sĩ mới với status PENDING
        if (request.getNewSingers() != null && !request.getNewSingers().isEmpty()) {
            for (NewSingerInfo newSingerInfo : request.getNewSingers()) {
                if (singerRepository.existsByEmail(newSingerInfo.getEmail())) {
                    throw new ResourceAlreadyExistsException("A singer with email '" + newSingerInfo.getEmail() + "' already exists.");
                }
                Singer newSinger = Singer.builder()
                        .name(newSingerInfo.getName())
                        .email(newSingerInfo.getEmail())
                        .avatarPath(newSingerInfo.getAvatarPath())
                        .creator(creator)
                        .status(SingerStatus.PENDING)
                        .build();
                allSingersForSubmission.add(singerRepository.save(newSinger));
            }
        }

        // 3. Xử lý và xác thực các ca sĩ đã có
        if (request.getExistingSingerIds() != null && !request.getExistingSingerIds().isEmpty()) {
            List<Singer> existingSingers = singerRepository.findAllById(request.getExistingSingerIds());
            for (Singer singer : existingSingers) {
                boolean isApproved = singer.getStatus() == SingerStatus.APPROVED;
                boolean isOwnPending = singer.getStatus() == SingerStatus.PENDING &&
                        singer.getCreator() != null &&
                        singer.getCreator().getId().equals(creator.getId());
                if (!(isApproved || isOwnPending)) {
                    throw new UnauthorizedException("You do not have permission to use the singer: " + singer.getName());
                }
            }
            allSingersForSubmission.addAll(existingSingers);
        }

        // 4. Xử lý tags
        Set<Tag> tags = new HashSet<>();
        if (request.getTagIds() != null) {
            tags.addAll(tagRepository.findAllById(request.getTagIds()));
        }

        // <<< BƯỚC 5 ĐÃ SỬA: Tạo thực thể SongSubmission >>>
        // Giả định rằng bạn đã thêm @Builder.Default và khởi tạo new HashSet<>() trong Entity
        SongSubmission submission = SongSubmission.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .filePath(request.getFilePath())
                .thumbnailPath(request.getThumbnailPath())
                .isPremium(request.getIsPremium())
                .creator(creator)
                .build();

        // <<< BƯỚC 6 ĐÃ SỬA: Tạo các liên kết và thêm vào collection của đối tượng cha >>>
        for (Singer singer : allSingersForSubmission) {
            SubmissionSingers submissionSinger = SubmissionSingers.builder()
                    .submission(submission)
                    .singer(singer)
                    .build();
            // Thêm trực tiếp vào collection của đối tượng cha
            submission.getSubmissionSingers().add(submissionSinger);
        }

        for (Tag tag : tags) {
            SubmissionTags submissionTag = SubmissionTags.builder()
                    .submission(submission)
                    .tag(tag)
                    .build();
            submission.getSubmissionTags().add(submissionTag);
        }

        // <<< BƯỚC 7 ĐÃ SỬA: Lưu đối tượng cha, CascadeType.ALL sẽ lo phần còn lại >>>
        SongSubmission savedSubmission = submissionRepository.save(submission);

        // <<< BƯỚC 8 ĐÃ SỬA: Không cần gọi findById nữa, trả về DTO từ đối tượng đã lưu >>>
        return submissionMapper.toDto(savedSubmission, creator);
    }

    // =================================================================
    // CÁC PHƯƠNG THỨC KHÁC GIỮ NGUYÊN, KHÔNG THAY ĐỔI
    // =================================================================

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

        if (!submission.getCreator().getId().equals(user.getId()) && !hasAdminRole(user)) {
            throw new UnauthorizedException("You don't have permission to access this submission");
        }

        return submissionMapper.toDto(submission, user);
    }

    @Transactional
    public void deleteSubmission(Long id, User user) {
        SongSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + id));

        if (!submission.getCreator().getId().equals(user.getId()) && !hasAdminRole(user)) {
            throw new UnauthorizedException("You don't have permission to delete this submission");
        }

        if (submission.getStatus() != SongSubmission.SubmissionStatus.PENDING) {
            throw new BadRequestException("Can only delete pending submissions");
        }

        submissionRepository.delete(submission);
    }

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

            Set<Singer> singers = submission.getSubmissionSingers().stream()
                    .map(SubmissionSingers::getSinger)
                    .collect(Collectors.toSet());

            Set<Tag> tags = submission.getSubmissionTags().stream()
                    .map(SubmissionTags::getTag)
                    .collect(Collectors.toSet());

            Song approvedSong = Song.builder()
                    .title(submission.getTitle())
                    .description(submission.getDescription())
                    .filePath(submission.getFilePath())
                    .thumbnailPath(submission.getThumbnailPath())
                    .isPremium(submission.getIsPremium())
                    .status(Song.SongStatus.APPROVED) // Trạng thái bài hát được duyệt
                    .creator(submission.getCreator()) // Gán người tạo bài hát
                    .submission(submission)
                    .singers(singers)
                    .tags(tags)
                    .build();

            Song savedSong = songRepository.save(approvedSong);
            submission.setApprovedSong(savedSong);

            submission.getSubmissionSingers().stream()
                    .map(SubmissionSingers::getSinger)
                    .filter(singer -> singer.getStatus() == Singer.SingerStatus.PENDING)
                    .forEach(singer -> {
                        singer.setStatus(Singer.SingerStatus.APPROVED);
                        singerRepository.save(singer);
                    });

        } else if ("REJECT".equalsIgnoreCase(request.getAction())) {
            submission.setStatus(SongSubmission.SubmissionStatus.REJECTED);
            submission.setRejectionReason(request.getRejectionReason());
        } else {
            throw new BadRequestException("Invalid review action. Must be APPROVE or REJECT");
        }

        SongSubmission updatedSubmission = submissionRepository.save(submission);
        return submissionMapper.toDto(updatedSubmission, reviewer);
    }

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

        if (!submission.getCreator().getId().equals(user.getId()) && !hasAdminRole(user)) {
            throw new UnauthorizedException("You don't have permission to update this submission");
        }

        if (submission.getStatus() != SongSubmission.SubmissionStatus.PENDING) {
            throw new BadRequestException("Can only update pending submissions");
        }

        submission.setTitle(request.getTitle());
        submission.setDescription(request.getDescription());
        submission.setFilePath(request.getFilePath());
        submission.setThumbnailPath(request.getThumbnailPath());
        submission.setIsPremium(request.getIsPremium());

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

    private <T> PagedResponse<T> createPagedResponse(Page<T> page) {
        return PagedResponse.of(page.getContent(), page);
    }
}