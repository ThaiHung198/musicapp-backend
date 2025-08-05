package com.musicapp.backend.repository;

import com.musicapp.backend.entity.Playlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    
    // Find user's playlists
    Page<Playlist> findByCreatorIdOrderByCreatedAtDesc(Long creatorId, Pageable pageable);
    
    // Find public playlists (admin created)
    @Query("SELECT p FROM Playlist p WHERE p.visibility = 'PUBLIC' ORDER BY p.createdAt DESC")
    Page<Playlist> findPublicPlaylistsOrderByCreatedAtDesc(Pageable pageable);
    
    // Find admin playlists (creator is null)
    @Query("SELECT p FROM Playlist p WHERE p.creator IS NULL ORDER BY p.createdAt DESC")
    Page<Playlist> findAdminPlaylistsOrderByCreatedAtDesc(Pageable pageable);
    
    // Search playlists
    @Query("SELECT p FROM Playlist p WHERE p.visibility = 'PUBLIC' AND " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY p.createdAt DESC")
    Page<Playlist> searchPublicPlaylists(@Param("keyword") String keyword, Pageable pageable);
    
    // Most liked playlists
    @Query("SELECT p FROM Playlist p WHERE p.visibility = 'PUBLIC' ORDER BY SIZE(p.likes) DESC")
    List<Playlist> findMostLikedPlaylists(Pageable pageable);
    
    // Recently created public playlists
    @Query("SELECT p FROM Playlist p WHERE p.visibility = 'PUBLIC' ORDER BY p.createdAt DESC")
    List<Playlist> findRecentlyCreatedPublicPlaylists(Pageable pageable);
    
    // Count user's playlists
    long countByCreatorId(Long creatorId);
    
    // Count public playlists
    @Query("SELECT COUNT(p) FROM Playlist p WHERE p.visibility = 'PUBLIC'")
    long countPublicPlaylists();
}
