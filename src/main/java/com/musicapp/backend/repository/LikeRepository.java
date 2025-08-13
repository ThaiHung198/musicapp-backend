package com.musicapp.backend.repository;

import com.musicapp.backend.entity.Like;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // <--- THÊM IMPORT NÀY
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    // Check if user liked an item
    Optional<Like> findByUserIdAndLikeableIdAndLikeableType(Long userId, Long likeableId, Like.LikeableType likeableType);

    boolean existsByUserIdAndLikeableIdAndLikeableType(Long userId, Long likeableId, Like.LikeableType likeableType);

    // Count likes for an item
    long countByLikeableIdAndLikeableType(Long likeableId, Like.LikeableType likeableType);

    // Delete like
    @Modifying // <--- THÊM ANNOTATION NÀY
    void deleteByUserIdAndLikeableIdAndLikeableType(Long userId, Long likeableId, Like.LikeableType likeableType);

    // Count total likes by user
    long countByUserId(Long userId);

    // Find user's liked songs
    @Query("SELECT l FROM Like l WHERE l.user.id = :userId AND l.likeableType = 'SONG' ORDER BY l.createdAt DESC")
    java.util.List<Like> findUserLikedSongs(@Param("userId") Long userId);

    // Find user's liked playlists
    @Query("SELECT l FROM Like l WHERE l.user.id = :userId AND l.likeableType = 'PLAYLIST' ORDER BY l.createdAt DESC")
    java.util.List<Like> findUserLikedPlaylists(@Param("userId") Long userId);

    /**
     * Tìm ID của các bài hát được yêu thích nhất, sắp xếp theo số lượt thích giảm dần.
     * @param pageable Giới hạn số lượng kết quả trả về.
     * @return Danh sách ID của các bài hát.
     */
    @Query("SELECT l.likeableId FROM Like l WHERE l.likeableType = 'SONG' GROUP BY l.likeableId ORDER BY COUNT(l.id) DESC")
    List<Long> findMostLikedSongIds(Pageable pageable);
}