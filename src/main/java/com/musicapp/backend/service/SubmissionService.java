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
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
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
    private final NotificationRepository notificationRepository;

    @Transactional
    public SubmissionDto createSubmission(CreateSubmissionRequest request, MultipartFile audioFile, MultipartFile thumbnailFile, List<MultipartFile> newSingerAvatars, String username) {
        User creator = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email: " + username));

        String audioFilePath = fileStorageService.storeFile(audioFile, "audio");
        String thumbnailFilePath = (thumbnailFile != null && !thumbnailFile.isEmpty())
                ? fileStorageService.storeFile(thumbnailFile, "images/songs")
                : null;

        if (CollectionUtils.isEmpty(request.getExistingSingerIds()) && CollectionUtils.isEmpty(request.getNewSingers())) {
            throw new BadRequestException("Cần có ít nhất một ca sĩ đã tồn tại hoặc ca sĩ mới.");
        }

        String description = request.getDescription();
        if (!StringUtils.hasText(description)) {
            description = "Bài hát này không có mô tả";
        }

        SongSubmission submission = SongSubmission.builder()
                .title(request.getTitle())
                .description(description)
                .lyrics(request.getLyrics())

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

        notifyAdminsOfNewSubmission(savedSubmission, creator);

        return submissionMapper.toDto(savedSubmission, creator);
    }

    private Set<Singer> processSingers(CreateSubmissionRequest request, List<MultipartFile> newSingerAvatars, User creator) {
        Set<Singer> allSingers = new HashSet<>();
        if (!CollectionUtils.isEmpty(request.getNewSingers())) {
            Map<String, MultipartFile> avatarFilesMap = (newSingerAvatars != null)
                    ? newSingerAvatars.stream().collect(Collectors.toMap(MultipartFile::getOriginalFilename, Function.identity(), (first, second) -> first))
                    : Collections.emptyMap();

            for (NewSingerInfo newSingerInfo : request.getNewSingers()) {
                if (singerRepository.existsByEmail(newSingerInfo.getEmail())) {
                    throw new ResourceAlreadyExistsException("Một ca sĩ với email '" + newSingerInfo.getEmail() + "' đã tồn tại.");
                }

                String avatarPath = null;
                if (StringUtils.hasText(newSingerInfo.getAvatarFileName()) && avatarFilesMap.containsKey(newSingerInfo.getAvatarFileName())) {
                    MultipartFile avatarFile = avatarFilesMap.get(newSingerInfo.getAvatarFileName());
                    avatarPath = fileStorageService.storeFile(avatarFile, "images/singers");
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
                    throw new UnauthorizedException("Bạn không có quyền sử dụng ca sĩ: " + singer.getName());
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

    @Transactional(readOnly = true)
    public PagedResponse<SubmissionDto> getSubmissionsByUser(String username, String keyword, SongSubmission.SubmissionStatus status, Pageable pageable) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email: " + username));

        Page<SongSubmission> submissionsPage;

        if (StringUtils.hasText(keyword)) {
            submissionsPage = submissionRepository.findByCreatorIdAndTitleContainingIgnoreCaseOrderBySubmissionDateDesc(user.getId(), keyword, pageable);
        } else if (status != null) {
            submissionsPage = submissionRepository.findByCreatorIdAndStatusOrderBySubmissionDateDesc(user.getId(), status, pageable);
        } else {
            submissionsPage = submissionRepository.findByCreatorIdOrderBySubmissionDateDesc(user.getId(), pageable);
        }

        List<SubmissionDto> submissionDtos = submissionsPage.getContent().stream()
                .map(sub -> submissionMapper.toDto(sub, user))
                .collect(Collectors.toList());

        return PagedResponse.of(submissionDtos, submissionsPage);
    }


    @Transactional(readOnly = true)
    public SubmissionDto getSubmissionById(Long id, String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email: " + username));

        SongSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu với ID: " + id));

        if (!submission.getCreator().getId().equals(user.getId()) && !user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedException("Bạn không có quyền truy cập vào yêu cầu này.");
        }

        return submissionMapper.toDto(submission, user);
    }

    @Transactional
    public SubmissionDto updateSubmission(Long id, CreateSubmissionRequest request, MultipartFile audioFile, MultipartFile thumbnailFile, String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email: " + username));

        SongSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu với ID: " + id));

        if (!submission.getCreator().getId().equals(user.getId())) {
            throw new UnauthorizedException("Bạn không có quyền cập nhật yêu cầu này.");
        }
        if (submission.getStatus() != SongSubmission.SubmissionStatus.PENDING) {
            throw new BadRequestException("Chỉ có thể cập nhật các yêu cầu đang ở trạng thái PENDING (Chờ duyệt).");
        }

        if (audioFile != null && !audioFile.isEmpty()) {
            fileStorageService.deleteFile(submission.getFilePath());
            String newAudioPath = fileStorageService.storeFile(audioFile, "audio");
            submission.setFilePath(newAudioPath);
        }
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            if (submission.getThumbnailPath() != null) {
                fileStorageService.deleteFile(submission.getThumbnailPath());
            }
            String newThumbnailPath = fileStorageService.storeFile(thumbnailFile, "images/songs");
            submission.setThumbnailPath(newThumbnailPath);
        }

        submission.setTitle(request.getTitle());
        String description = request.getDescription();
        if (!StringUtils.hasText(description)) {
            submission.setDescription("Bài hát này không có mô tả");
        } else {
            submission.setDescription(description);
        }
        submission.setLyrics(request.getLyrics());

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
                            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca sĩ đang chờ duyệt với ID " + singerInfo.getId() + "."));
                    if (singerToProcess.getStatus() != SingerStatus.PENDING || singerToProcess.getCreator() == null || !singerToProcess.getCreator().getId().equals(user.getId())) {
                        throw new UnauthorizedException("Bạn không có quyền chỉnh sửa ca sĩ với ID " + singerInfo.getId());
                    }
                    Optional<Singer> singerWithSameEmail = singerRepository.findByEmail(singerInfo.getEmail());
                    if (singerWithSameEmail.isPresent() && !singerWithSameEmail.get().getId().equals(singerInfo.getId())) {
                        throw new ResourceAlreadyExistsException("Email '" + singerInfo.getEmail() + "' đã được sử dụng bởi một ca sĩ khác.");
                    }
                    singerToProcess.setName(singerInfo.getName());
                    singerToProcess.setEmail(singerInfo.getEmail());
                } else {
                    if (singerRepository.existsByEmail(singerInfo.getEmail())) {
                        throw new ResourceAlreadyExistsException("Một ca sĩ với email '" + singerInfo.getEmail() + "' đã tồn tại.");
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
                    throw new UnauthorizedException("Bạn không có quyền sử dụng ca sĩ: " + singer.getName());
                }
            }
            allSingersForSubmission.addAll(existingSingers);
        }
        return allSingersForSubmission;
    }

    @Transactional
    public void deleteSubmission(Long id, String username) {
        User creator = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email: " + username));

        SongSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu với ID: " + id));

        if (!submission.getCreator().getId().equals(creator.getId())) {
            throw new UnauthorizedException("Bạn không có quyền rút lại yêu cầu này.");
        }
        if (submission.getStatus() != SongSubmission.SubmissionStatus.PENDING) {
            throw new BadRequestException("Bạn chỉ có thể rút lại các yêu cầu đang ở trạng thái PENDING (Chờ duyệt).");
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
                    if (singer.getAvatarPath() != null) {
                        fileStorageService.deleteFile(singer.getAvatarPath());
                    }
                    singerRepository.delete(singer);
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public PagedResponse<SubmissionDto> getSubmissionsByStatus(SongSubmission.SubmissionStatus status, Pageable pageable) {
        Page<SongSubmission> page = submissionRepository.findByStatusOrderBySubmissionDateDesc(status, pageable);
        List<SubmissionDto> dtos = page.getContent().stream()
                .map(sub -> submissionMapper.toDto(sub, null))
                .collect(Collectors.toList());
        return PagedResponse.of(dtos, page);
    }

    @Transactional(readOnly = true)
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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người duyệt với email: " + reviewerUsername));

        SongSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu với ID: " + id));

        if (submission.getStatus() != SongSubmission.SubmissionStatus.PENDING) {
            throw new BadRequestException("Chỉ có thể duyệt các yêu cầu đang chờ xử lý.");
        }

        submission.setReviewer(reviewer);
        submission.setReviewDate(LocalDateTime.now());

        User creator = submission.getCreator();

        Set<Singer> associatedSingers = submission.getSubmissionSingers().stream()
                .map(SubmissionSingers::getSinger)
                .collect(Collectors.toSet());

        if ("APPROVE".equalsIgnoreCase(request.getAction())) {
            submission.setStatus(SongSubmission.SubmissionStatus.APPROVED);

            Set<Tag> tags = submission.getSubmissionTags().stream()
                    .map(SubmissionTags::getTag)
                    .collect(Collectors.toSet());

            Song approvedSong = Song.builder()
                    .title(submission.getTitle())
                    .description(submission.getDescription())
                    .lyrics(submission.getLyrics())
                    .filePath(submission.getFilePath())
                    .thumbnailPath(submission.getThumbnailPath())
                    .isPremium(submission.getIsPremium())
                    .status(Song.SongStatus.APPROVED)
                    .creator(submission.getCreator())
                    .submission(submission)
                    .singers(associatedSingers)
                    .tags(tags)
                    .build();

            Song savedSong = songRepository.save(approvedSong);
            submission.setApprovedSong(savedSong);

            associatedSingers.stream()
                    .filter(singer -> singer.getStatus() == Singer.SingerStatus.PENDING)
                    .forEach(singer -> {
                        singer.setStatus(Singer.SingerStatus.APPROVED);
                        singerRepository.save(singer);
                    });

            Notification notification = Notification.builder()
                    .recipient(creator)
                    .actor(reviewer)
                    .type(Notification.NotificationType.SUBMISSION_APPROVED)
                    .message("Yêu cầu của bạn cho bài hát '" + submission.getTitle() + "' đã được duyệt.")
                    .link("/song/" + savedSong.getId())
                    .build();
            notificationRepository.save(notification);

        } else if ("REJECT".equalsIgnoreCase(request.getAction())) {
            submission.setStatus(SongSubmission.SubmissionStatus.REJECTED);
            submission.setRejectionReason(request.getRejectionReason());

            associatedSingers.stream()
                    .filter(singer -> singer.getStatus() == Singer.SingerStatus.PENDING)
                    .forEach(singer -> {
                        singer.setStatus(Singer.SingerStatus.REJECTED);
                        singerRepository.save(singer);
                    });

            Notification notification = Notification.builder()
                    .recipient(creator)
                    .actor(reviewer)
                    .type(Notification.NotificationType.SUBMISSION_REJECTED)
                    .message("Yêu cầu của bạn cho bài hát '" + submission.getTitle() + "' đã bị từ chối. Lý do: " + request.getRejectionReason())
                    .link("/creator/my-submissions")
                    .build();
            notificationRepository.save(notification);

            SongSubmission updatedSubmission = submissionRepository.save(submission);

            return submissionMapper.toDto(updatedSubmission, reviewer);

        } else {
            throw new BadRequestException("Hành động duyệt không hợp lệ. Phải là APPROVE (Duyệt) hoặc REJECT (Từ chối).");
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

    @Transactional(readOnly = true)
    public Object getUserSubmissionStats(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email: " + username));

        return Map.of(
                "totalSubmissions", submissionRepository.countByCreatorId(user.getId()),
                "pendingSubmissions", submissionRepository.countByCreatorIdAndStatus(user.getId(), SongSubmission.SubmissionStatus.PENDING),
                "approvedSubmissions", submissionRepository.countByCreatorIdAndStatus(user.getId(), SongSubmission.SubmissionStatus.APPROVED),
                "rejectedSubmissions", submissionRepository.countByCreatorIdAndStatus(user.getId(), SongSubmission.SubmissionStatus.REJECTED)
        );
    }

    @Transactional(readOnly = true)
    public Object getAdminSubmissionStats() {
        return Map.of(
                "totalSubmissions", submissionRepository.count(),
                "pendingSubmissions", submissionRepository.countByStatus(SongSubmission.SubmissionStatus.PENDING),
                "approvedSubmissions", submissionRepository.countByStatus(SongSubmission.SubmissionStatus.APPROVED),
                "rejectedSubmissions", submissionRepository.countByStatus(SongSubmission.SubmissionStatus.REJECTED),
                "reviewingSubmissions", submissionRepository.countByStatus(SongSubmission.SubmissionStatus.REVIEWING)
        );
    }

    private void notifyAdminsOfNewSubmission(SongSubmission submission, User creator) {
        List<User> admins = userRepository.findByRoleNameAndKeyword("ROLE_ADMIN", "", Pageable.unpaged()).getContent();

        if (admins.isEmpty()) {
            System.out.println("Cảnh báo: Không tìm thấy quản trị viên nào để thông báo cho yêu cầu mới có ID: " + submission.getId());
            return;
        }

        List<Notification> notifications = new ArrayList<>();
        String message = creator.getDisplayName() + " vừa gửi một bài hát mới để duyệt: " + submission.getTitle();
        String link = "/admin/submissions";

        for (User admin : admins) {
            Notification notification = Notification.builder()
                    .recipient(admin)
                    .actor(creator)
                    .type(Notification.NotificationType.NEW_SUBMISSION_PENDING)
                    .message(message)
                    .link(link)
                    .build();
            notifications.add(notification);
        }

        notificationRepository.saveAll(notifications);
    }
}