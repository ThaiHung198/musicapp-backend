package com.musicapp.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "chart_ranking")
public class ChartRanking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false, unique = true) // Mỗi bài hát chỉ có 1 rank
    private Song song;

    @Column(name = "current_rank", nullable = false)
    private int currentRank;

    @Column(name = "previous_rank")
    private Integer previousRank; // Nullable for new entries

    @Column(nullable = false)
    private double score;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}