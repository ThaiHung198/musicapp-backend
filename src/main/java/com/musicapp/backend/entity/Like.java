package com.musicapp.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "likes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "likeable_id", "likeable_type"})
})
public class Like {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "likeable_id", nullable = false)
    private Long likeableId;
    
    @Column(name = "likeable_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private LikeableType likeableType;
    
    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Helper relationships (for easier queries)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "likeable_id", insertable = false, updatable = false)
    private Song song;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "likeable_id", insertable = false, updatable = false)
    private Playlist playlist;
    
    public enum LikeableType {
        SONG, PLAYLIST
    }
}
