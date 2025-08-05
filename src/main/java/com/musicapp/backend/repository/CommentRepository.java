package com.musicapp.backend.repository;

import com.musicapp.backend.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // Find comments for a specific item
    Page<Comment> findByCommentableIdAndCommentableTypeOrderByCreatedAtDesc(
        Long commentableId, 
        Comment.CommentableType commentableType, 
        Pageable pageable
    );
    
    // Find user's comments
    Page<Comment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // Count comments for an item
    long countByCommentableIdAndCommentableType(Long commentableId, Comment.CommentableType commentableType);
    
    // Count user's comments
    long countByUserId(Long userId);
    
    // Find recent comments (for admin notifications)
    @Query("SELECT c FROM Comment c ORDER BY c.createdAt DESC")
    Page<Comment> findRecentComments(Pageable pageable);
    
    // Check if user can delete comment (owner or admin)
    @Query("SELECT c FROM Comment c WHERE c.id = :commentId AND c.user.id = :userId")
    java.util.Optional<Comment> findByIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);
}
