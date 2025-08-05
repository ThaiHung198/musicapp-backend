package com.musicapp.backend.repository;

import com.musicapp.backend.entity.Singer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SingerRepository extends JpaRepository<Singer, Long> {
    
    Optional<Singer> findByNameIgnoreCase(String name);
    
    boolean existsByNameIgnoreCase(String name);
    
    @Query("SELECT s FROM Singer s ORDER BY s.name ASC")
    Page<Singer> findAllOrderByNameAsc(Pageable pageable);
    
    @Query("SELECT s FROM Singer s WHERE s.name LIKE %:keyword% ORDER BY s.name ASC")
    Page<Singer> findByNameContainingIgnoreCaseOrderByNameAsc(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT COUNT(song) FROM Song song JOIN song.singers s WHERE s.id = :singerId")
    Long countSongsBySingerId(@Param("singerId") Long singerId);
}
