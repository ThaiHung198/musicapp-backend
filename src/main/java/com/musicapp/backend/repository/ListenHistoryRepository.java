package com.musicapp.backend.repository;

import com.musicapp.backend.entity.ListenHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ListenHistoryRepository extends JpaRepository<ListenHistory, Long> {

    @Query("SELECT COUNT(lh.id) FROM ListenHistory lh WHERE lh.song.id = :songId AND lh.listenTimestamp >= :since")
    long countListensForSongSince(@Param("songId") Long songId, @Param("since") LocalDateTime since);

}