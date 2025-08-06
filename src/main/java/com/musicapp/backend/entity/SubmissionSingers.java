package com.musicapp.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "submission_singers")
public class SubmissionSingers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private SongSubmission submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "singer_id", nullable = false)
    private Singer singer;

    // Unique constraint to prevent duplicate associations
    @Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"submission_id", "singer_id"})
    })
    public static class SubmissionSingersConstraints {}
}
