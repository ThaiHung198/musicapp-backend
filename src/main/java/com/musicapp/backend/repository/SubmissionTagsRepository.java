package com.musicapp.backend.repository;

import com.musicapp.backend.entity.SubmissionTags;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionTagsRepository extends JpaRepository<SubmissionTags, Long> {
    
    @Query("SELECT st FROM SubmissionTags st WHERE st.submission.id = :submissionId")
    List<SubmissionTags> findBySubmissionId(@Param("submissionId") Long submissionId);
    
    @Query("SELECT st FROM SubmissionTags st WHERE st.tag.id = :tagId")
    List<SubmissionTags> findByTagId(@Param("tagId") Long tagId);
    
    @Query("SELECT st FROM SubmissionTags st WHERE st.submission.id = :submissionId AND st.tag.id = :tagId")
    SubmissionTags findBySubmissionIdAndTagId(@Param("submissionId") Long submissionId, @Param("tagId") Long tagId);

    @Modifying
    @Query("DELETE FROM SubmissionTags st WHERE st.submission.id = :submissionId")
    void deleteBySubmissionId(@Param("submissionId") Long submissionId);
    
    @Query("SELECT COUNT(st) FROM SubmissionTags st WHERE st.tag.id = :tagId")
    long countByTagId(@Param("tagId") Long tagId);
    
    @Query("SELECT st.tag.id FROM SubmissionTags st WHERE st.submission.id = :submissionId")
    List<Long> findTagIdsBySubmissionId(@Param("submissionId") Long submissionId);
    
    @Query("SELECT st.submission.id FROM SubmissionTags st WHERE st.tag.id = :tagId")
    List<Long> findSubmissionIdsByTagId(@Param("tagId") Long tagId);
}
