package com.musicapp.backend.service;

import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.submission.CreateSubmissionRequest;
import com.musicapp.backend.dto.submission.CreateSubmissionRequest.NewSingerInfo;
import com.musicapp.backend.dto.submission.ReviewSubmissionRequest;
import com.musicapp.backend.dto.submission.SubmissionDto;
import com.musicapp.backend.entity.*;
import com.musicapp.backend.entity.Singer.SingerStatus;
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
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
    public SubmissionDto createSubmission(CreateSubmissionRequest request, MultipartFile audioFile, MultipartFile thumbnailFile, List<MultipartFile> newSingerAvatars, String username) {
        User creator = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));

        String audioFilePath = fileStorageService.storeFile(audioFile, "audio");
        String thumbnailFilePath = null;
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            thumbnailFilePath = fileStorageService.storeFile(thumbnailFile, "images/songs");
        }

        if (CollectionUtils.isEmpty(request.getExistingSingerIds()) && CollectionUtils.isEmpty(request.getNewSingers())) {
            throw new BadRequestException("At least one existing or new singer is required.");
        }

        SongSubmission submission = SongSubmission.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .filePath(audioFilePath)
                .thumbnailPath(thumbnailFilePath)
                .isPremium(request.getIsPremium())
                .creator(creator)
                .status(SongSubmission.SubmissionStatus.PENDING)
                .build();

        Set<Singer> allSingersForSubmission = processSingers(request, newSingerAvatars, creator);
        Set<Tag> tags = new HashSet<>();
        if (request.getTagIds() != null) {
            tags.addAll(tagRepository.findAllById(request.getTagIds()));
        }

        for (Singer singer : allSingersForSubmission) {
            submission.getSubmissionSingers().add(SubmissionSingers.builder().submission(submission).singer(singer).build());
        }
        for (Tag tag : tags) {
            submission.getSubmissionTags().add(SubmissionTags.builder().submission(submission).tag(tag).build());
        }

        SongSubmission savedSubmission = submissionRepository.save(submission);

        return submissionMapper.toDto(savedSubmission, creator);
    }

    private Set<Singer> processSingers(CreateSubmissionRequest request, List<MultipartFile> newSingerAvatars, User creator) {
        Set<Singer> allSingers = new HashSet<>();
        if (!CollectionUtils.isEmpty(request.getNewSingers())) {
            int avatarIndex = 0;
            for (NewSingerInfo newSingerInfo : request.getNewSingers()) {
                if (singerRepository.existsByEmail(newSingerInfo.getEmail())) {
                    throw new ResourceAlreadyExistsException("A singer with email '" + newSingerInfo.getEmail() + "' already exists.");
                }

                String avatarPath = null;
                if (newSingerAvatars != null && avatarIndex < newSingerAvatars.size()) {
                    MultipartFile avatarFile = newSingerAvatars.get(avatarIndex);
                    if (avatarFile != null && !avatarFile.isEmpty()) {
                        avatarPath = fileStorageService.storeFile(avatarFile, "images/singers");
                    }
                    avatarIndex++;
                }

                Singer newSinger = Singer.builder()
                        .name(newSingerInfo.getName())
                        .email(newSingerInfo.getEmail())
                        .avatarPath(avatarPath)
                        .creator(creator)
                        .status(Singer.SingerStatus.PENDING)
                        .build();
                allSingers.add(singerRepository.save(newSinger));
            }
        }

        if (!CollectionUtils.isEmpty(request.getExistingSingerIds())) {
            List<Singer> existingSingers = singerRepository.findAllById(request.getExistingSingerIds());
            for (Singer singer : existingSingers) {
                boolean isApproved = singer.getStatus() == Singer.SingerStatus.APPROVED;
                boolean isOwnPending = singer.getStatus() == Singer.SingerStatus.PENDING &&
                        singer.getCreator() != null &&
                        singer.getCreator().getId().equals(creator.getId());

                boolean isOwnRejected = singer.getStatus() == Singer.SingerStatus.REJECTED &&
                        singer.getCreator() != null &&
                        singer.getCreator().getId().equals(creator.getId());

                if (!(isApproved || isOwnPending || isOwnRejected)) {
                    throw new UnauthorizedException("You do not have permission to use the singer: " + singer.getName());
                }

                if (isOwnRejected) {
                    singer.setStatus(Singer.SingerStatus.PENDING);
                    singerRepository.save(singer);
                }
            }
            allSingers.addAll(existingSingers);
        }
        return allSingers;
    }

    public PagedResponse<SubmissionDto> getSubmissionsByUser(String username, SongSubmission.SubmissionStatus status, Pageable pageable) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));

        Page<SongSubmission> submissionsPage;
        if (status != null) {
            submissionsPage = submissionRepository.findByCreatorIdAndStatusOrderBySubmissionDateDesc(user.getId(), status, pageable);
        } else {
            submissionsPage = submissionRepository.findByCreatorIdOrderBySubmissionDateDesc(user.getId(), pageable);
        }

        List<SubmissionDto> submissionDtos = submissionsPage.getContent().stream()
                .map(sub -> submissionMapper.toDto(sub, user))
                .collect(Collectors.toList());

        return PagedResponse.of(submissionDtos, submissionsPage);
    }

    public SubmissionDto getSubmissionById(Long id, String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));

        SongSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + id));

        if (!submission.getCreator().getId().equals(user.getId()) && !user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedException("You don't have permission to access this submission");
        }

        return submissionMapper.toDto(submission, user);
    }

    @Transactional
    public SubmissionDto updateSubmission(Long id, CreateSubmissionRequest request, String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));

        SongSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + id));

        if (!submission.getCreator().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to update this submission");
        }
        if (submission.getStatus() != SongSubmission.SubmissionStatus.PENDING) {
            throw new BadRequestException("Can only update submissions that are in PENDING status.");
        }

        submission.setTitle(request.getTitle());
        submission.setDescription(request.getDescription());
        submission.setIsPremium(request.getIsPremium());

        Set<Singer> oldSingers = submission.getSubmissionSingers().stream()
                .map(SubmissionSingers::getSinger)
                .collect(Collectors.toSet());

        Set<Singer> newSingersForSubmission = processSingersForUpdate(request, user);

        submission.getSubmissionSingers().clear();
        submissionSingersRepository.deleteBySubmissionId(id);

        for (Singer singer : newSingersForSubmission) {
            submission.getSubmissionSingers().add(SubmissionSingers.builder().submission(submission).singer(singer).build());
        }

        Set<Singer> removedSingers = new HashSet<>(oldSingers);
        removedSingers.removeAll(newSingersForSubmission);
        cleanupOrphanedPendingSingers(removedSingers);

        submission.getSubmissionTags().clear();
        submissionTagsRepository.deleteBySubmissionId(id);
        if (!CollectionUtils.isEmpty(request.getTagIds())) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            for (Tag tag : tags) {
                submission.getSubmissionTags().add(SubmissionTags.builder().submission(submission).tag(tag).build());
            }
        }

        SongSubmission updatedSubmission = submissionRepository.save(submission);
        return submissionMapper.toDto(updatedSubmission, user);
    }

    private Set<Singer> processSingersForUpdate(CreateSubmissionRequest request, User user) {
        Set<Singer> allSingersForSubmission = new HashSet<>();

        if (!CollectionUtils.isEmpty(request.getNewSingers())) {
            for (NewSingerInfo singerInfo : request.getNewSingers()) {
                Singer singerToProcess;
                if (singerInfo.getId() != null) {
                    singerToProcess = singerRepository.findById(singerInfo.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Pending singer with id " + singerInfo.getId() + " not found."));
                    if (singerToProcess.getStatus() != SingerStatus.PENDING || singerToProcess.getCreator() == null || !singerToProcess.getCreator().getId().equals(user.getId())) {
                        throw new UnauthorizedException("You do not have permission to modify singer with id " + singerInfo.getId());
                    }
                    singerRepository.findByEmail(singerInfo.getEmail()).ifPresent(otherSinger -> {
                        if (!otherSinger.getId().equals(singerInfo.getId())) {
                            throw new ResourceAlreadyExistsException("Email '" + singerInfo.getEmail() + "' is already used by another singer.");
                        }
                    });
                    singerToProcess.setName(singerInfo.getName());
                    singerToProcess.setEmail(singerInfo.getEmail());
                } else {
                    if (singerRepository.existsByEmail(singerInfo.getEmail())) {
                        throw new ResourceAlreadyExistsException("A singer with email '" + singerInfo.getEmail() + "' already exists.");
                    }
                    singerToProcess = Singer.builder()
                            .name(singerInfo.getName())
                            .email(singerInfo.getEmail())
                            .creator(user)
                            .status(SingerStatus.PENDING)
                            .build();
                }
                allSingersForSubmission.add(singerRepository.save(singerToProcess));
            }
        }

        if (!CollectionUtils.isEmpty(request.getExistingSingerIds())) {
            List<Singer> existingSingers = singerRepository.findAllById(request.getExistingSingerIds());
            for (Singer singer : existingSingers) {
                boolean isApproved = singer.getStatus() == Singer.SingerStatus.APPROVED;
                boolean isOwn = singer.getCreator() != null && singer.getCreator().getId().equals(user.getId());
                if (!(isApproved || isOwn)) {
                    throw new UnauthorizedException("You do not have permission to use the singer: " + singer.getName());
                }
            }
            allSingersForSubmission.addAll(existingSingers);
        }
        return allSingersForSubmission;
    }

    @Transactional
    public void deleteSubmission(Long id, String username) {
        User creator = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));

        SongSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + id));

        if (!submission.getCreator().getId().equals(creator.getId())) {
            throw new UnauthorizedException("You do not have permission to withdraw this submission.");
        }
        if (submission.getStatus() != SongSubmission.SubmissionStatus.PENDING) {
            throw new BadRequestException("You can only withdraw submissions that are in PENDING status.");
        }

        Set<Singer> associatedSingers = submission.getSubmissionSingers().stream()
                .map(SubmissionSingers::getSinger)
                .collect(Collectors.toSet());

        submissionRepository.delete(submission);

        cleanupOrphanedPendingSingers(associatedSingers);
    }

    private void cleanupOrphanedPendingSingers(Set<Singer> singers) {
        for (Singer singer : singers) {
            if (singer.getStatus() == Singer.SingerStatus.PENDING) {
                long submissionCount = submissionSingersRepository.countBySingerId(singer.getId());
                if (submissionCount == 0) {
                    singerRepository.delete(singer);
                }
            }
        }
    }

    public PagedResponse<SubmissionDto> getSubmissionsByStatus(SongSubmission.SubmissionStatus status, Pageable pageable) {
        Page<SongSubmission> page = submissionRepository.findByStatusOrderBySubmissionDateDesc(status, pageable);
        List<SubmissionDto> dtos = page.getContent().stream()
                .map(sub -> submissionMapper.toDto(sub, null))
                .collect(Collectors.toList());
        return PagedResponse.of(dtos, page);
    }

    public PagedResponse<SubmissionDto> searchSubmissions(String search, SongSubmission.SubmissionStatus status, Pageable pageable) {
        Page<SongSubmission> submissionsPage;
        if (search != null && !search.trim().isEmpty()) {
            submissionsPage = submissionRepository.searchByTitleAndStatus(search.trim(), status, pageable);
        } else {
            submissionsPage = submissionRepository.findByStatusOrderBySubmissionDateDesc(status, pageable);
        }
        List<SubmissionDto> dtos = submissionsPage.getContent().stream()
                .map(sub -> submissionMapper.toDto(sub, null))
                .collect(Collectors.toList());
        return PagedResponse.of(dtos, submissionsPage);
    }

    @Transactional
    public SubmissionDto reviewSubmission(Long id, ReviewSubmissionRequest request, String reviewerUsername) {
        User reviewer = userRepository.findByEmail(reviewerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found with email: " + reviewerUsername));

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

            submission.getSubmissionSingers().stream()
                    .map(SubmissionSingers::getSinger)
                    .filter(singer -> singer.getStatus() == Singer.SingerStatus.PENDING)
                    .forEach(singer -> {
                        singer.setStatus(Singer.SingerStatus.REJECTED);
                        singerRepository.save(singer);
                    });
        } else {
            throw new BadRequestException("Invalid review action. Must be APPROVE or REJECT");
        }

        SongSubmission updatedSubmission = submissionRepository.save(submission);
        return submissionMapper.toDto(updatedSubmission, reviewer);
    }

    @Transactional
    public SubmissionDto approveSubmission(Long id, String reviewerUsername) {
        ReviewSubmissionRequest request = ReviewSubmissionRequest.builder()
                .action("APPROVE")
                .build();
        return reviewSubmission(id, request, reviewerUsername);
    }

    @Transactional
    public SubmissionDto rejectSubmission(Long id, String reason, String reviewerUsername) {
        ReviewSubmissionRequest request = ReviewSubmissionRequest.builder()
                .action("REJECT")
                .rejectionReason(reason)
                .build();
        return reviewSubmission(id, request, reviewerUsername);
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
}