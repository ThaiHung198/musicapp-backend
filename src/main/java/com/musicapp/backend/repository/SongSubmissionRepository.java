package com.musicapp.backend.repository;

import com.musicapp.backend.entity.SongSubmission;
import com.musicapp.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SongSubmissionRepository extends JpaRepository<SongSubmission, Long> {

    Page<SongSubmission> findByCreatorIdOrderBySubmissionDateDesc(Long creatorId, Pageable pageable);

    Page<SongSubmission> findByStatusOrderBySubmissionDateDesc(SongSubmission.SubmissionStatus status, Pageable pageable);

    @Query("SELECT s FROM SongSubmission s WHERE s.status = 'PENDING' ORDER BY s.submissionDate ASC")
    Page<SongSubmission> findPendingSubmissions(Pageable pageable);

    Page<SongSubmission> findByCreatorIdAndStatusOrderBySubmissionDateDesc(
            Long creatorId, SongSubmission.SubmissionStatus status, Pageable pageable);

    @Query("SELECT s FROM SongSubmission s WHERE s.creator.id = :creatorId AND LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY s.submissionDate DESC")
    Page<SongSubmission> findByCreatorIdAndTitleContainingIgnoreCaseOrderBySubmissionDateDesc(@Param("creatorId") Long creatorId, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT s FROM SongSubmission s WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "AND s.status = :status ORDER BY s.submissionDate DESC")
    Page<SongSubmission> searchByTitleAndStatus(@Param("keyword") String keyword,
                                                @Param("status") SongSubmission.SubmissionStatus status,
                                                Pageable pageable);

    long countByStatus(SongSubmission.SubmissionStatus status);

    long countByCreatorId(Long creatorId);

    long countByCreatorIdAndStatus(Long creatorId, SongSubmission.SubmissionStatus status);

    @Query("SELECT s FROM SongSubmission s WHERE s.isPremium = true AND s.status = :status ORDER BY s.submissionDate DESC")
    Page<SongSubmission> findPremiumSubmissions(@Param("status") SongSubmission.SubmissionStatus status, Pageable pageable);

    @Query("SELECT s FROM SongSubmission s WHERE s.submissionDate BETWEEN :startDate AND :endDate ORDER BY s.submissionDate DESC")
    Page<SongSubmission> findBySubmissionDateBetween(@Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate,
                                                     Pageable pageable);

    @Query("SELECT s FROM SongSubmission s ORDER BY s.submissionDate DESC")
    List<SongSubmission> findRecentSubmissions(Pageable pageable);

    Page<SongSubmission> findByReviewerIdOrderByReviewDateDesc(Long reviewerId, Pageable pageable);

    @Query("SELECT DISTINCT s FROM SongSubmission s " +
            "JOIN FETCH s.creator " +
            "LEFT JOIN FETCH s.submissionSingers ss " +
            "LEFT JOIN FETCH ss.singer " +
            "LEFT JOIN FETCH s.submissionTags st " +
            "LEFT JOIN FETCH st.tag " +
            "WHERE s.id = :id")
    Optional<SongSubmission> findByIdWithAllRelations(@Param("id") Long id);
}