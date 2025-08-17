package com.musicapp.backend.repository;

import com.musicapp.backend.entity.Playlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    List<Playlist> findByCreatorId(Long creatorId);

    Page<Playlist> findByVisibilityOrderByListenCountDesc(Playlist.PlaylistVisibility visibility, Pageable pageable);

    @Query("SELECT p FROM Playlist p WHERE p.visibility = 'PUBLIC' ORDER BY p.createdAt DESC")
    Page<Playlist> findRecentlyCreatedPublicPlaylists(Pageable pageable);

    @Query("SELECT p FROM Playlist p WHERE p.visibility = 'PUBLIC' ORDER BY p.listenCount DESC")
    List<Playlist> findTopListenedPublicPlaylists(Pageable pageable);

    @Query("SELECT p FROM Playlist p JOIN p.creator u JOIN u.roles r WHERE r.name = 'ROLE_CREATOR'")
    List<Playlist> findPlaylistsByCreators();

    @Modifying
    @Query("UPDATE Playlist p SET p.listenCount = p.listenCount + 1 WHERE p.id = :playlistId")
    void incrementListenCount(@Param("playlistId") Long playlistId);

    @Query("SELECT p FROM Playlist p WHERE p.visibility = 'PUBLIC' AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Playlist> searchPublicPlaylistsByName(@Param("keyword") String keyword, Pageable pageable);

    @Modifying
    void deleteByCreatorIdAndVisibility(Long creatorId, Playlist.PlaylistVisibility visibility);
}