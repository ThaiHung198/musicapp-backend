package com.musicapp.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "playlist_comments")
public class PlaylistComment extends BaseComment {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    public PlaylistComment(User user, String content, Playlist playlist) {
        this.setUser(user);
        this.setContent(content);
        this.playlist = playlist;
        this.setCreatedAt(java.time.LocalDateTime.now());
    }
}