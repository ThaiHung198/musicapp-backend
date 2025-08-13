package com.musicapp.backend.repository;

import com.musicapp.backend.entity.PlaylistComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaylistCommentRepository extends JpaRepository<PlaylistComment, Long> {
    Page<PlaylistComment> findByPlaylistIdOrderByCreatedAtDesc(Long playlistId, Pageable pageable);
}