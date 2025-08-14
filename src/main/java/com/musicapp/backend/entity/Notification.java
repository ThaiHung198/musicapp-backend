package com.musicapp.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người nhận thông báo (VD: creator của bài hát)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    // Người thực hiện hành động (VD: người đã like/comment)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type; // LIKE, COMMENT

    @Column(columnDefinition = "TEXT")
    private String message; // Nội dung thông báo, VD: "Nguyễn Văn A đã thích bài hát của bạn."

    @Column(name = "is_read")
    @Builder.Default
    private boolean isRead = false;

    // Link để khi click vào thông báo sẽ chuyển đến (VD: link bài hát/playlist)
    @Column(name = "link_to_content")
    private String link;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum NotificationType {
        SONG_LIKE,
        PLAYLIST_LIKE,
        SONG_COMMENT,
        PLAYLIST_COMMENT,
        SUBMISSION_APPROVED,
        SUBMISSION_REJECTED,
        NEW_SUBMISSION_PENDING
    }
}