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

@Repository
public interface SongSubmissionRepository extends JpaRepository<SongSubmission, Long> {
    
    // Find by creator
    Page<SongSubmission> findByCreatorIdOrderBySubmissionDateDesc(Long creatorId, Pageable pageable);
    
    // Find by status
    Page<SongSubmission> findByStatusOrderBySubmissionDateDesc(SongSubmission.SubmissionStatus status, Pageable pageable);
    
    // Find pending submissions for admin review
    @Query("SELECT s FROM SongSubmission s WHERE s.status = 'PENDING' ORDER BY s.submissionDate ASC")
    Page<SongSubmission> findPendingSubmissions(Pageable pageable);
    
    // Find by creator and status
    Page<SongSubmission> findByCreatorIdAndStatusOrderBySubmissionDateDesc(
            Long creatorId, SongSubmission.SubmissionStatus status, Pageable pageable);
    
    // Search submissions by title
    @Query("SELECT s FROM SongSubmission s WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND s.status = :status ORDER BY s.submissionDate DESC")
    Page<SongSubmission> searchByTitleAndStatus(@Param("keyword") String keyword, 
                                                @Param("status") SongSubmission.SubmissionStatus status, 
                                                Pageable pageable);
    
    // Count submissions by status
    long countByStatus(SongSubmission.SubmissionStatus status);
    
    // Count submissions by creator
    long countByCreatorId(Long creatorId);
    
    // Count submissions by creator and status
    long countByCreatorIdAndStatus(Long creatorId, SongSubmission.SubmissionStatus status);
    
    // Premium submissions
    @Query("SELECT s FROM SongSubmission s WHERE s.isPremium = true AND s.status = :status ORDER BY s.submissionDate DESC")
    Page<SongSubmission> findPremiumSubmissions(@Param("status") SongSubmission.SubmissionStatus status, Pageable pageable);
    
    // Submissions within date range
    @Query("SELECT s FROM SongSubmission s WHERE s.submissionDate BETWEEN :startDate AND :endDate ORDER BY s.submissionDate DESC")
    Page<SongSubmission> findBySubmissionDateBetween(@Param("startDate") LocalDateTime startDate, 
                                                     @Param("endDate") LocalDateTime endDate, 
                                                     Pageable pageable);
    
    // Recent submissions for dashboard
    @Query("SELECT s FROM SongSubmission s ORDER BY s.submissionDate DESC")
    List<SongSubmission> findRecentSubmissions(Pageable pageable);
    
    // Find submissions reviewed by admin
    Page<SongSubmission> findByReviewerIdOrderByReviewDateDesc(Long reviewerId, Pageable pageable);
}
