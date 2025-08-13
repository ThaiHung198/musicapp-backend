package com.musicapp.backend.repository;

import com.musicapp.backend.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {

    // Basic CRUD
    Optional<Song> findByIdAndStatus(Long id, Song.SongStatus status);

    // Find by creator
    Page<Song> findByCreatorIdOrderByCreatedAtDesc(Long creatorId, Pageable pageable);

    // Find by status
    Page<Song> findByStatusOrderByCreatedAtDesc(Song.SongStatus status, Pageable pageable);

    // Search functionality
    @Query("SELECT s FROM Song s WHERE s.status = :status AND " +
            "(LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "EXISTS (SELECT 1 FROM s.singers singer WHERE LOWER(singer.name) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
    Page<Song> searchApprovedSongs(@Param("keyword") String keyword,
                                   @Param("status") Song.SongStatus status,
                                   Pageable pageable);

    // Top songs by listen count
    @Query("SELECT s FROM Song s WHERE s.status = 'APPROVED' ORDER BY s.listenCount DESC")
    List<Song> findTopSongsByListenCount(Pageable pageable);

    // Recently created approved songs
    @Query("SELECT s FROM Song s WHERE s.status = 'APPROVED' ORDER BY s.createdAt DESC")
    List<Song> findRecentlyCreatedSongs(Pageable pageable);

    // --- BẮT ĐẦU SỬA LỖI ---
    // Đã xóa phương thức findMostLikedSongs vì Song entity không còn thuộc tính 'likes' nữa.
    // Chức năng này sẽ được xây dựng lại ở tầng Service nếu cần.
    // --- KẾT THÚC SỬA LỖI ---

    // Songs by singer
    @Query("SELECT s FROM Song s JOIN s.singers singer WHERE singer.id = :singerId AND s.status = 'APPROVED' ORDER BY s.createdAt DESC")
    Page<Song> findBySingerIdAndApproved(@Param("singerId") Long singerId, Pageable pageable);

    // Songs by tag
    @Query("SELECT s FROM Song s JOIN s.tags tag WHERE tag.id = :tagId AND s.status = 'APPROVED' ORDER BY s.createdAt DESC")
    Page<Song> findByTagIdAndApproved(@Param("tagId") Long tagId, Pageable pageable);

    // Increment listen count
    @Modifying
    @Query("UPDATE Song s SET s.listenCount = s.listenCount + 1 WHERE s.id = :songId")
    void incrementListenCount(@Param("songId") Long songId);

    // Count songs by status
    long countByStatus(Song.SongStatus status);

    // Count songs by creator
    long countByCreatorId(Long creatorId);

    // Premium content queries
    Page<Song> findByIsPremiumTrueAndStatusOrderByCreatedAtDesc(Song.SongStatus status, Pageable pageable);
    Page<Song> findByIsPremiumFalseAndStatusOrderByCreatedAtDesc(Song.SongStatus status, Pageable pageable);

    @Query("SELECT s FROM Song s " +
            "JOIN FETCH s.creator " +
            "LEFT JOIN FETCH s.singers " +
            "LEFT JOIN FETCH s.tags " +
            "WHERE s.id = :id AND s.status = :status")
    Optional<Song> findByIdAndStatusWithDetails(@Param("id") Long id, @Param("status") Song.SongStatus status);

    Page<Song> findByCreatorIdAndStatusOrderByCreatedAtDesc(Long creatorId, Song.SongStatus status, Pageable pageable);
    @Query("SELECT s FROM Song s " +
            "WHERE s.creator.id = :creatorId " +
            "AND s.status = :status " +
            "AND LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY s.createdAt DESC")
    Page<Song> searchByTitleForCreatorAndStatus(
            @Param("keyword") String keyword,
            @Param("creatorId") Long creatorId,
            @Param("status") Song.SongStatus status,
            Pageable pageable
    );

    @Query("SELECT s FROM Song s WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY s.createdAt DESC")
    Page<Song> searchAllSongsByTitle(@Param("keyword") String keyword, Pageable pageable);

    Page<Song> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT s FROM Song s JOIN s.playlists p " +
            "WHERE p.id = :playlistId AND s.status = 'APPROVED' " +
            "AND s.title LIKE CONCAT('%', :keyword, '%')") // <-- ĐÃ BỎ LOWER()
    Page<Song> findApprovedSongsForPlaylist(
            @Param("playlistId") Long playlistId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    long countByCreatorIdAndStatus(Long creatorId, Song.SongStatus status);
}