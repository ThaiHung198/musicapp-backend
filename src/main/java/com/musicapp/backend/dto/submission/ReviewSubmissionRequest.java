package com.musicapp.backend.dto.submission;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSubmissionRequest {
    
    @NotBlank(message = "Review action is required")
    private String action; // "APPROVE" or "REJECT"
    
    private String rejectionReason; // Required if action is REJECT
}
