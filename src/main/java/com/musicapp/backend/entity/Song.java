package com.musicapp.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "songs")
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ... các cột khác giữ nguyên ...
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "thumbnail_path")
    private String thumbnailPath;

    @Column(name = "listen_count")
    @Builder.Default
    private Long listenCount = 0L;

    @Column(name = "is_premium")
    @Builder.Default
    private Boolean isPremium = false;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SongStatus status = SongStatus.PENDING;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User creator;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "song_singers", joinColumns = @JoinColumn(name = "song_id"), inverseJoinColumns = @JoinColumn(name = "singer_id"))
    @Builder.Default
    private Set<Singer> singers = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "song_tags", joinColumns = @JoinColumn(name = "song_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany(mappedBy = "songs", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Playlist> playlists = new HashSet<>();

    // --- BẮT ĐẦU SỬA LỖI ---
    // ĐÃ XÓA MỐI QUAN HỆ VỚI LIKE ĐỂ PHÙ HỢP VỚI THIẾT KẾ ĐA HÌNH
    // Việc lấy/đếm like sẽ được thực hiện qua LikeRepository.
    // --- KẾT THÚC SỬA LỖI ---

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private SongSubmission submission;

    public enum SongStatus {
        PENDING,  // Đang chờ duyệt
        APPROVED, // Đã duyệt, đang hiển thị
        REJECTED, // Bị từ chối
        HIDDEN    // Đã duyệt, nhưng đang bị ẩn
    }

    // ... các phương thức equals, hashCode, toString giữ nguyên ...
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return id != null && id.equals(song.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status=" + status +
                '}';
    }
}