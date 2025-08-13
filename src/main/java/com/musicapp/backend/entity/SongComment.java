package com.musicapp.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "song_comments")
public class SongComment extends BaseComment {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    public SongComment(User user, String content, Song song) {
        this.setUser(user);
        this.setContent(content);
        this.song = song;
        this.setCreatedAt(java.time.LocalDateTime.now());
    }
}