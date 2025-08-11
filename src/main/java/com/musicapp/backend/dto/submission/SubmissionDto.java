package com.musicapp.backend.dto.submission;

import com.musicapp.backend.dto.singer.SingerDto;
import com.musicapp.backend.dto.tag.TagDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionDto {
    private Long id;
    private String title;
    private String description;
    private String filePath;
    private String thumbnailPath;
    private Boolean isPremium;
//    private BigDecimal premiumPrice;
    private String status;
    private LocalDateTime submissionDate;
    private LocalDateTime reviewDate;
    private String rejectionReason;
    
    // Creator info
    private Long creatorId;
    private String creatorName;
    private String creatorEmail;
    
    // Reviewer info (for admin view)
    private Long reviewerId;
    private String reviewerName;
    
    // Associated data
    private List<SingerDto> singers;
    private List<TagDto> tags;
    
    // Approved song info (if approved)
    private Long approvedSongId;
    
    // Permissions
    private Boolean canEdit;
    private Boolean canReview;
    private Boolean canDelete;
}
