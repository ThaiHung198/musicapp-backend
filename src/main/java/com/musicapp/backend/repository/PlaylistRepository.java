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

import com.musicapp.backend.entity.Playlist.PlaylistVisibility;
import org.springframework.data.jpa.repository.Modifying;
import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    Page<Playlist> findByCreatorIdOrderByCreatedAtDesc(Long creatorId, Pageable pageable);

    @Query("SELECT p FROM Playlist p WHERE p.visibility = 'PUBLIC' ORDER BY p.createdAt DESC")
    Page<Playlist> findPublicPlaylistsOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT p FROM Playlist p WHERE p.creator IS NULL ORDER BY p.createdAt DESC")
    Page<Playlist> findAdminPlaylistsOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT p FROM Playlist p WHERE p.visibility = 'PUBLIC' AND " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY p.createdAt DESC")
    Page<Playlist> searchPublicPlaylists(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Playlist p WHERE p.visibility = 'PUBLIC' ORDER BY p.createdAt DESC")
    List<Playlist> findRecentlyCreatedPublicPlaylists(Pageable pageable);

    long countByCreatorId(Long creatorId);

    @Query("SELECT COUNT(p) FROM Playlist p WHERE p.visibility = 'PUBLIC'")
    long countPublicPlaylists();

    Page<Playlist> findByVisibilityOrderByListenCountDesc(Playlist.PlaylistVisibility visibility, Pageable pageable);
    @Modifying
    void deleteByCreatorIdAndVisibility(Long creatorId, PlaylistVisibility visibility);

    List<Playlist> findByCreatorId(Long creatorId);

    @Query("SELECT p FROM Playlist p JOIN p.creator u JOIN u.roles r WHERE r.name = 'ROLE_CREATOR'")
    List<Playlist> findPlaylistsByCreators();

    @Modifying
    @Query("UPDATE Playlist p SET p.listenCount = p.listenCount + 1 WHERE p.id = :playlistId")
    void incrementListenCount(@Param("playlistId") Long playlistId);

    @Query("SELECT p FROM Playlist p WHERE p.visibility = 'PUBLIC' ORDER BY p.listenCount DESC")
    List<Playlist> findTopListenedPublicPlaylists(Pageable pageable);
}