package com.musicapp.backend.service;

import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.submission.CreateSubmissionRequest;
import com.musicapp.backend.dto.submission.CreateSubmissionRequest.NewSingerInfo;
import com.musicapp.backend.dto.submission.ReviewSubmissionRequest;
import com.musicapp.backend.dto.submission.SubmissionDto;
import com.musicapp.backend.entity.*;
import com.musicapp.backend.exception.BadRequestException;
import com.musicapp.backend.exception.ResourceAlreadyExistsException;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
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
    private final SubmissionSingersRepository submissionSingersRepository;
    private final SubmissionTagsRepository submissionTagsRepository;
    private final SubmissionMapper submissionMapper;
    private final FileStorageService fileStorageService;

    @Transactional
    public SubmissionDto createSubmission(CreateSubmissionRequest request, MultipartFile audioFile, MultipartFile thumbnailFile, String username) {
        User creator = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));

        String audioFilePath = fileStorageService.storeFile(audioFile, "audio");
        String thumbnailFilePath = null;
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            thumbnailFilePath = fileStorageService.storeFile(thumbnailFile, "images");
        }

        if ((request.getExistingSingerIds() == null || request.getExistingSingerIds().isEmpty()) &&
                (request.getNewSingers() == null || request.getNewSingers().isEmpty())) {
            throw new BadRequestException("At least one existing or new singer is required.");
        }
        Set<Singer> allSingersForSubmission = processSingers(request, creator);

        Set<Tag> tags = new HashSet<>();
        if (request.getTagIds() != null) {
            tags.addAll(tagRepository.findAllById(request.getTagIds()));
        }

        SongSubmission submission = SongSubmission.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .filePath(audioFilePath)
                .thumbnailPath(thumbnailFilePath)
                .isPremium(request.getIsPremium())
                .creator(creator)
                .build();

        for (Singer singer : allSingersForSubmission) {
            submission.getSubmissionSingers().add(SubmissionSingers.builder().submission(submission).singer(singer).build());
        }
        for (Tag tag : tags) {
            submission.getSubmissionTags().add(SubmissionTags.builder().submission(submission).tag(tag).build());
        }

        SongSubmission savedSubmission = submissionRepository.save(submission);

        return submissionMapper.toDto(savedSubmission, creator);
    }

    private Set<Singer> processSingers(CreateSubmissionRequest request, User creator) {
        Set<Singer> allSingers = new HashSet<>();
        if (request.getNewSingers() != null) {
            for (NewSingerInfo newSingerInfo : request.getNewSingers()) {
                if (singerRepository.existsByEmail(newSingerInfo.getEmail())) {
                    throw new ResourceAlreadyExistsException("A singer with email '" + newSingerInfo.getEmail() + "' already exists.");
                }
                Singer newSinger = Singer.builder()
                        .name(newSingerInfo.getName())
                        .email(newSingerInfo.getEmail())
                        .creator(creator)
                        .status(Singer.SingerStatus.PENDING)
                        .build();
                allSingers.add(singerRepository.save(newSinger));
            }
        }
        if (request.getExistingSingerIds() != null) {
            List<Singer> existingSingers = singerRepository.findAllById(request.getExistingSingerIds());
            for (Singer singer : existingSingers) {
                boolean isApproved = singer.getStatus() == Singer.SingerStatus.APPROVED;
                boolean isOwnPending = singer.getStatus() == Singer.SingerStatus.PENDING &&
                        singer.getCreator() != null &&
                        singer.getCreator().getId().equals(creator.getId());

                // Định nghĩa điều kiện cho phép sử dụng ca sĩ đã bị từ chối >>>
                boolean isOwnRejected = singer.getStatus() == Singer.SingerStatus.REJECTED &&
                        singer.getCreator() != null &&
                        singer.getCreator().getId().equals(creator.getId());

                if (!(isApproved || isOwnPending || isOwnRejected)) {
                    throw new UnauthorizedException("You do not have permission to use the singer: " + singer.getName());
                }

                // Nếu ca sĩ bị từ chối được dùng lại, chuyển status về PENDING >>>
                if (isOwnRejected) {
                    singer.setStatus(Singer.SingerStatus.PENDING);
                    singerRepository.save(singer); // Lưu lại trạng thái mới
                }
            }
            allSingers.addAll(existingSingers);
        }
        return allSingers;
    }

    @Transactional
    public SubmissionDto createSubmission(CreateSubmissionRequest request, User creator) {
        if ((request.getExistingSingerIds() == null || request.getExistingSingerIds().isEmpty()) &&
                (request.getNewSingers() == null || request.getNewSingers().isEmpty())) {
            throw new BadRequestException("At least one existing or new singer is required.");
        }

        Set<Singer> allSingersForSubmission = processSingers(request, creator);

        Set<Tag> tags = new HashSet<>();
        if (request.getTagIds() != null) {
            tags.addAll(tagRepository.findAllById(request.getTagIds()));
        }

        SongSubmission submission = SongSubmission.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .isPremium(request.getIsPremium())
                .creator(creator)
                .build();

        for (Singer singer : allSingersForSubmission) {
            SubmissionSingers submissionSinger = SubmissionSingers.builder()
                    .submission(submission)
                    .singer(singer)
                    .build();
            submission.getSubmissionSingers().add(submissionSinger);
        }

        for (Tag tag : tags) {
            SubmissionTags submissionTag = SubmissionTags.builder()
                    .submission(submission)
                    .tag(tag)
                    .build();
            submission.getSubmissionTags().add(submissionTag);
        }

        SongSubmission savedSubmission = submissionRepository.save(submission);
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
                    .status(Song.SongStatus.APPROVED)
                    .creator(submission.getCreator())
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

            // Khi từ chối bài hát, từ chối luôn các ca sĩ PENDING đi kèm
            submission.getSubmissionSingers().stream()
                    .map(SubmissionSingers::getSinger)
                    .filter(singer -> singer.getStatus() == Singer.SingerStatus.PENDING)
                    .forEach(singer -> {
                        singer.setStatus(Singer.SingerStatus.REJECTED); // Đổi status sang REJECTED
                        singerRepository.save(singer);
                    });
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

        // ... kiểm tra quyền và trạng thái (không đổi) ...
        if (!submission.getCreator().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to update this submission");
        }
        if (submission.getStatus() != SongSubmission.SubmissionStatus.PENDING) {
            throw new BadRequestException("Can only update submissions that are in PENDING status.");
        }


        // ... cập nhật các trường cơ bản (không đổi) ...
        submission.setTitle(request.getTitle());
        submission.setDescription(request.getDescription());
        submission.setIsPremium(request.getIsPremium());

        // ... xóa các liên kết cũ (không đổi) ...
        submissionSingersRepository.deleteBySubmissionId(id);
        submissionTagsRepository.deleteBySubmissionId(id);
        submission.getSubmissionSingers().clear();
        submission.getSubmissionTags().clear();

        // ======================== LOGIC MỚI SỬ DỤNG ID ========================

        Set<Singer> allSingersForSubmission = new HashSet<>();

        if (!CollectionUtils.isEmpty(request.getNewSingers())) {
            for (NewSingerInfo singerInfo : request.getNewSingers()) {
                Singer singerToProcess;

                if (singerInfo.getId() != null) {
                    // === TRƯỜNG HỢP 1: CẬP NHẬT ca sĩ PENDING đã có ID ===
                    singerToProcess = singerRepository.findById(singerInfo.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Pending singer with id " + singerInfo.getId() + " not found."));

                    // Kiểm tra quyền sở hữu và trạng thái
                    if (singerToProcess.getStatus() != SingerStatus.PENDING ||
                            singerToProcess.getCreator() == null ||
                            !singerToProcess.getCreator().getId().equals(user.getId())) {
                        throw new UnauthorizedException("You do not have permission to modify singer with id " + singerInfo.getId());
                    }

                    // Kiểm tra email mới có bị trùng với người khác không
                    singerRepository.findByEmail(singerInfo.getEmail()).ifPresent(otherSinger -> {
                        if (!otherSinger.getId().equals(singerInfo.getId())) {
                            throw new ResourceAlreadyExistsException("Email '" + singerInfo.getEmail() + "' is already used by another singer.");
                        }
                    });

                    // Cập nhật thông tin
                    singerToProcess.setName(singerInfo.getName());
                    singerToProcess.setEmail(singerInfo.getEmail()); // Bây giờ có thể cập nhật email
                    singerToProcess.setAvatarPath(singerInfo.getAvatarPath());

                } else {
                    // === TRƯỜNG HỢP 2: TẠO MỚI ca sĩ (không có ID) ===
                    if (singerRepository.existsByEmail(singerInfo.getEmail())) {
                        throw new ResourceAlreadyExistsException("A singer with email '" + singerInfo.getEmail() + "' already exists.");
                    }
                    singerToProcess = Singer.builder()
                            .name(singerInfo.getName())
                            .email(singerInfo.getEmail())
                            .avatarPath(singerInfo.getAvatarPath())
                            .creator(user)
                            .status(SingerStatus.PENDING)
                            .build();
                }

                allSingersForSubmission.add(singerRepository.save(singerToProcess));
            }
        }

        // ... xử lý existingSingerIds và tags (không đổi) ...
        if (!CollectionUtils.isEmpty(request.getExistingSingerIds())) {
            // ... logic cũ ...
            List<Singer> existingSingers = singerRepository.findAllById(request.getExistingSingerIds());
            for (Singer singer : existingSingers) {
//                boolean isApproved = singer.getStatus() == SingerStatus.APPROVED;
//                boolean isOwnPending = singer.getStatus() == SingerStatus.PENDING &&
//                        singer.getCreator() != null &&
//                        singer.getCreator().getId().equals(user.getId());
                if (singer.getStatus() != SingerStatus.APPROVED) {
                    throw new UnauthorizedException("You can only use singers with APPROVED status.");
                }
            }
            allSingersForSubmission.addAll(existingSingers);
        }

        // ... tạo liên kết submission_singers và submission_tags ...
        for (Singer singer : allSingersForSubmission) {
            SubmissionSingers submissionSinger = SubmissionSingers.builder()
                    .submission(submission)
                    .singer(singer)
                    .build();
            submission.getSubmissionSingers().add(submissionSinger);
        }

        if (!CollectionUtils.isEmpty(request.getTagIds())) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            for (Tag tag : tags) {
                SubmissionTags submissionTag = SubmissionTags.builder()
                        .submission(submission)
                        .tag(tag)
                        .build();
                submission.getSubmissionTags().add(submissionTag);
            }
        }

        SongSubmission updatedSubmission = submissionRepository.save(submission);
        return submissionMapper.toDto(updatedSubmission, user);
    }

    @Transactional
    public void deleteSubmission(Long id, String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));

        SongSubmission submission = submissionRepository.findByIdWithAllRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + id));

        if (!submission.getCreator().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not have permission to withdraw this submission.");
        }
        if (submission.getStatus() != SongSubmission.SubmissionStatus.PENDING) {
            throw new BadRequestException("You can only withdraw submissions that are in PENDING status.");
        }

        List<Singer> singersToDelete = new ArrayList<>();

        Set<SubmissionSingers> submissionSingersSet = new HashSet<>(submission.getSubmissionSingers());

        for (SubmissionSingers ss : submissionSingersSet) {
            Singer singer = ss.getSinger();
            if (singer != null && singer.getStatus() == Singer.SingerStatus.PENDING) {
                long submissionCount = submissionSingersRepository.countBySingerId(singer.getId());
                if (submissionCount <= 1) {
                    singersToDelete.add(singer);
                }
            }
        }

        submissionRepository.delete(submission);

        submissionRepository.flush();

        if (!singersToDelete.isEmpty()) {
            singerRepository.deleteAll(singersToDelete);
        }

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

    @Transactional
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