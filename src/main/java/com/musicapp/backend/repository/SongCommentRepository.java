package com.musicapp.backend.repository;

import com.musicapp.backend.entity.SongComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SongCommentRepository extends JpaRepository<SongComment, Long> {

    Page<SongComment> findBySongIdOrderByCreatedAtDesc(Long songId, Pageable pageable);

    long countBySongId(Long songId);

}