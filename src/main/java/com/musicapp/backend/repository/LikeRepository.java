// backend/src/main/java/com/musicapp/backend/repository/LikeRepository.java
package com.musicapp.backend.repository;

import com.musicapp.backend.entity.Like;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUserIdAndLikeableIdAndLikeableType(Long userId, Long likeableId, Like.LikeableType likeableType);

    boolean existsByUserIdAndLikeableIdAndLikeableType(Long userId, Long likeableId, Like.LikeableType likeableType);

    long countByLikeableIdAndLikeableType(Long likeableId, Like.LikeableType likeableType);

    @Modifying
    void deleteByUserIdAndLikeableIdAndLikeableType(Long userId, Long likeableId, Like.LikeableType likeableType);

    long countByUserId(Long userId);

    @Query("SELECT l FROM Like l WHERE l.user.id = :userId AND l.likeableType = 'SONG' ORDER BY l.createdAt DESC")
    java.util.List<Like> findUserLikedSongs(@Param("userId") Long userId);

    @Query("SELECT l FROM Like l WHERE l.user.id = :userId AND l.likeableType = 'PLAYLIST' ORDER BY l.createdAt DESC")
    java.util.List<Like> findUserLikedPlaylists(@Param("userId") Long userId);

    @Query("SELECT l.likeableId FROM Like l WHERE l.likeableType = 'SONG' GROUP BY l.likeableId ORDER BY COUNT(l.id) DESC")
    List<Long> findMostLikedSongIds(Pageable pageable);

    @Query("SELECT l.likeableId FROM Like l WHERE l.likeableType = 'PLAYLIST' GROUP BY l.likeableId ORDER BY COUNT(l.id) DESC")
    List<Long> findMostLikedPlaylistIds(Pageable pageable);
}