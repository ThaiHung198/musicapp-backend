package com.musicapp.backend.repository;

import com.musicapp.backend.dto.playlist.PlaylistDto;
import com.musicapp.backend.entity.Playlist;
import com.musicapp.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    // --- BẮT ĐẦU SỬA LỖI ---
    // Đã xóa "CAST(SIZE(p.likes) AS long)" và "CAST(SIZE(p.comments) AS long)" khỏi câu truy vấn
    // vì các thuộc tính này không còn được map trong Entity nữa.
    @Query("SELECT new com.musicapp.backend.dto.playlist.PlaylistDto(" +
            "p.id, p.name, p.thumbnailPath, p.visibility, p.createdAt, " +
            "p.creator.id, " +
            "CAST(SIZE(p.songs) AS long)) " + // Chỉ còn lại songCount
            "FROM Playlist p WHERE p.creator = :creator ORDER BY p.createdAt DESC")
    List<PlaylistDto> findPlaylistsByCreator(@Param("creator") User creator);
    // --- KẾT THÚC SỬA LỖI ---


    Page<Playlist> findByCreatorIdOrderByCreatedAtDesc(Long creatorId, Pageable pageable);

    @Query("SELECT p FROM Playlist p WHERE p.visibility = 'PUBLIC' ORDER BY p.createdAt DESC")
    Page<Playlist> findPublicPlaylistsOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT p FROM Playlist p WHERE p.creator IS NULL ORDER BY p.createdAt DESC")
    Page<Playlist> findAdminPlaylistsOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT p FROM Playlist p WHERE p.visibility = 'PUBLIC' AND " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY p.createdAt DESC")
    Page<Playlist> searchPublicPlaylists(@Param("keyword") String keyword, Pageable pageable);

    // --- BẮT ĐẦU SỬA LỖI ---
    // Đã xóa phương thức findMostLikedPlaylists vì Playlist entity không còn thuộc tính 'likes' nữa.
    // --- KẾT THÚC SỬA LỖI ---

    @Query("SELECT p FROM Playlist p WHERE p.visibility = 'PUBLIC' ORDER BY p.createdAt DESC")
    List<Playlist> findRecentlyCreatedPublicPlaylists(Pageable pageable);

    long countByCreatorId(Long creatorId);

    @Query("SELECT COUNT(p) FROM Playlist p WHERE p.visibility = 'PUBLIC'")
    long countPublicPlaylists();

    Page<Playlist> findByVisibility(Playlist.PlaylistVisibility visibility, Pageable pageable);
}