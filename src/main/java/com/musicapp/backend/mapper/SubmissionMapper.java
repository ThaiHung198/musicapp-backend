package com.musicapp.backend.mapper;

import com.musicapp.backend.dto.submission.SubmissionDto;
import com.musicapp.backend.entity.SongSubmission;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.repository.SingerRepository;
import com.musicapp.backend.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SubmissionMapper {
    
    private final SingerMapper singerMapper;
    private final TagMapper tagMapper;
    
    public SubmissionDto toDto(SongSubmission submission, User currentUser) {
        if (submission == null) return null;
        
        boolean canEdit = currentUser != null && 
            submission.getCreator().getId().equals(currentUser.getId()) &&
            submission.getStatus() == SongSubmission.SubmissionStatus.PENDING;
            
        boolean canReview = currentUser != null && 
            hasAdminRole(currentUser) &&
            submission.getStatus() == SongSubmission.SubmissionStatus.PENDING;
            
        boolean canDelete = currentUser != null && 
            (submission.getCreator().getId().equals(currentUser.getId()) || hasAdminRole(currentUser));
        
        return SubmissionDto.builder()
                .id(submission.getId())
                .title(submission.getTitle())
                .description(submission.getDescription())
                .filePath(submission.getFilePath())
                .thumbnailPath(submission.getThumbnailPath())
                .isPremium(submission.getIsPremium())
                .status(submission.getStatus().name())
                .submissionDate(submission.getSubmissionDate())
                .reviewDate(submission.getReviewDate())
                .rejectionReason(submission.getRejectionReason())
                .creatorId(submission.getCreator().getId())
                .creatorName(submission.getCreator().getDisplayName())
                .creatorEmail(submission.getCreator().getEmail())
                .reviewerId(submission.getReviewer() != null ? submission.getReviewer().getId() : null)
                .reviewerName(submission.getReviewer() != null ? submission.getReviewer().getDisplayName() : null)
                .singers(submission.getSubmissionSingers() != null ?
                    submission.getSubmissionSingers().stream()
                        .map(ss -> singerMapper.toDtoWithoutSongCount(ss.getSinger()))
                        .collect(Collectors.toList()) : null)
                .tags(submission.getSubmissionTags() != null ?
                    submission.getSubmissionTags().stream()
                        .map(st -> tagMapper.toDto(st.getTag()))
                        .collect(Collectors.toList()) : null)
                .approvedSongId(submission.getApprovedSong() != null ? submission.getApprovedSong().getId() : null)
                .canEdit(canEdit)
                .canReview(canReview)
                .canDelete(canDelete)
                .build();
    }
    
    public SubmissionDto toDtoBasic(SongSubmission submission) {
        if (submission == null) return null;
        
        return SubmissionDto.builder()
                .id(submission.getId())
                .title(submission.getTitle())
                .description(submission.getDescription())
                .thumbnailPath(submission.getThumbnailPath())
                .isPremium(submission.getIsPremium())
                .status(submission.getStatus().name())
                .submissionDate(submission.getSubmissionDate())
                .creatorId(submission.getCreator().getId())
                .creatorName(submission.getCreator().getDisplayName())
                .build();
    }
    
    private boolean hasAdminRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
    }
}
