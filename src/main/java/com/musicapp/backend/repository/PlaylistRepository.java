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

    @Query("SELECT new com.musicapp.backend.dto.playlist.PlaylistDto(" +
            "p.id, p.name, p.thumbnailPath, p.visibility, p.createdAt, " +
            "p.creator.id, " +
            "CAST(SIZE(p.songs) AS long), " +
            "CAST(SIZE(p.likes) AS long), " +
            "CAST(SIZE(p.comments) AS long)) " +
            "FROM Playlist p WHERE p.creator = :creator ORDER BY p.createdAt DESC")
    List<PlaylistDto> findPlaylistsByCreator(@Param("creator") User creator);

    Page<Playlist> findByCreatorIdOrderByCreatedAtDesc(Long creatorId, Pageable pageable);

    @Query("SELECT p FROM Playlist p WHERE p.visibility = 'PUBLIC' ORDER BY p.createdAt DESC")
    Page<Playlist> findPublicPlaylistsOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT p FROM Playlist p WHERE p.creator IS NULL ORDER BY p.createdAt DESC")
    Page<Playlist> findAdminPlaylistsOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT p FROM Playlist p WHERE p.visibility = 'PUBLIC' AND " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY p.createdAt DESC")
    Page<Playlist> searchPublicPlaylists(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Playlist p WHERE p.visibility = 'PUBLIC' ORDER BY SIZE(p.likes) DESC")
    List<Playlist> findMostLikedPlaylists(Pageable pageable);

    @Query("SELECT p FROM Playlist p WHERE p.visibility = 'PUBLIC' ORDER BY p.createdAt DESC")
    List<Playlist> findRecentlyCreatedPublicPlaylists(Pageable pageable);

    long countByCreatorId(Long creatorId);

    @Query("SELECT COUNT(p) FROM Playlist p WHERE p.visibility = 'PUBLIC'")
    long countPublicPlaylists();

}