package com.musicapp.backend.repository;

import com.musicapp.backend.entity.SubmissionSingers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionSingersRepository extends JpaRepository<SubmissionSingers, Long> {
    
    @Query("SELECT ss FROM SubmissionSingers ss WHERE ss.submission.id = :submissionId")
    List<SubmissionSingers> findBySubmissionId(@Param("submissionId") Long submissionId);
    
    @Query("SELECT ss FROM SubmissionSingers ss WHERE ss.singer.id = :singerId")
    List<SubmissionSingers> findBySingerId(@Param("singerId") Long singerId);
    
    @Query("SELECT ss FROM SubmissionSingers ss WHERE ss.submission.id = :submissionId AND ss.singer.id = :singerId")
    SubmissionSingers findBySubmissionIdAndSingerId(@Param("submissionId") Long submissionId, @Param("singerId") Long singerId);

    @Modifying
    @Query("DELETE FROM SubmissionSingers ss WHERE ss.submission.id = :submissionId")
    void deleteBySubmissionId(@Param("submissionId") Long submissionId);
    
    @Query("SELECT COUNT(ss) FROM SubmissionSingers ss WHERE ss.singer.id = :singerId")
    long countBySingerId(@Param("singerId") Long singerId);
    
    @Query("SELECT ss.singer.id FROM SubmissionSingers ss WHERE ss.submission.id = :submissionId")
    List<Long> findSingerIdsBySubmissionId(@Param("submissionId") Long submissionId);
}
