package com.musicapp.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "playlists")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "thumbnail_path")
    private String thumbnailPath;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PlaylistVisibility visibility = PlaylistVisibility.PRIVATE;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User creator; // NULL for admin playlists

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "playlist_songs",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    @Builder.Default
    private Set<Song> songs = new HashSet<>();

    // Giữ nguyên mối quan hệ với Like
    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Like> likes = new HashSet<>();

    // --- BẮT ĐẦU SỬA LỖI ---
    // ĐÃ XÓA HOÀN TOÀN MỐI QUAN HỆ VỚI COMMENT ĐỂ TRÁNH LỖI KHÓA NGOẠI
    // Việc lấy comment sẽ được thực hiện qua CommentRepository
    // --- KẾT THÚC SỬA LỖI ---

    public enum PlaylistVisibility {
        PRIVATE, PUBLIC
    }
}