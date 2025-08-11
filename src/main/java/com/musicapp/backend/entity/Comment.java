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
@Table(name = "comments")
public class Comment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "commentable_id", nullable = false)
    private Long commentableId;
    
    @Column(name = "commentable_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CommentableType commentableType;
    
    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Helper relationships (for easier queries)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commentable_id", insertable = false, updatable = false)
    private Song song;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commentable_id", insertable = false, updatable = false)
    private Playlist playlist;
    
    public enum CommentableType {
        SONG, PLAYLIST
    }
}
