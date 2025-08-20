package com.musicapp.backend.repository;

import com.musicapp.backend.entity.ChartRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChartRankingRepository extends JpaRepository<ChartRanking, Long> {

    @Query("SELECT cr FROM ChartRanking cr " +
            "JOIN FETCH cr.song s " +
            "JOIN FETCH s.creator " +
            "LEFT JOIN FETCH s.singers " +
            "LEFT JOIN FETCH s.tags " +
            "ORDER BY cr.currentRank ASC")
    List<ChartRanking> findAllWithSongDetails();

    List<ChartRanking> findAllByOrderByCurrentRankAsc();
}