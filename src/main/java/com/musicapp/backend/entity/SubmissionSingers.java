package com.musicapp.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Getter;
import lombok.Setter;

//@Data
@Getter // <<< Dùng @Getter thay thế
@Setter // <<< Dùng @Setter thay thế
@ToString(exclude = {"submission"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "submission_singers", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"submission_id", "singer_id"})
})
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

    @Override
    public String toString() {
        return "SubmissionSingers{" +
                "id=" + id +
                // Chỉ in ID của cha và con để tránh vòng lặp
                ", submissionId=" + (submission != null ? submission.getId() : "null") +
                ", singerId=" + (singer != null ? singer.getId() : "null") +
                '}';
    }
}
