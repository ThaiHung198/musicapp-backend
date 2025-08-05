package com.musicapp.backend.repository;

import com.musicapp.backend.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    
    // Check if user liked an item
    Optional<Like> findByUserIdAndLikeableIdAndLikeableType(Long userId, Long likeableId, Like.LikeableType likeableType);
    
    boolean existsByUserIdAndLikeableIdAndLikeableType(Long userId, Long likeableId, Like.LikeableType likeableType);
    
    // Count likes for an item
    long countByLikeableIdAndLikeableType(Long likeableId, Like.LikeableType likeableType);
    
    // Delete like
    void deleteByUserIdAndLikeableIdAndLikeableType(Long userId, Long likeableId, Like.LikeableType likeableType);
    
    // Count total likes by user
    long countByUserId(Long userId);
    
    // Find user's liked songs
    @Query("SELECT l FROM Like l WHERE l.user.id = :userId AND l.likeableType = 'SONG' ORDER BY l.createdAt DESC")
    java.util.List<Like> findUserLikedSongs(@Param("userId") Long userId);
    
    // Find user's liked playlists
    @Query("SELECT l FROM Like l WHERE l.user.id = :userId AND l.likeableType = 'PLAYLIST' ORDER BY l.createdAt DESC")
    java.util.List<Like> findUserLikedPlaylists(@Param("userId") Long userId);
}
