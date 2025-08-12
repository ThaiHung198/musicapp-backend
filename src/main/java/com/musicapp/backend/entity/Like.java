package com.musicapp.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // --- BẮT ĐẦU SỬA LỖI ---
    // Đã xóa các mối quan hệ "helper" không cần thiết đến Song và Playlist
    // để tránh các vấn đề tiềm ẩn của JPA.
    // --- KẾT THÚC SỬA LỖI ---

    public enum LikeableType {
        SONG, PLAYLIST
    }
}