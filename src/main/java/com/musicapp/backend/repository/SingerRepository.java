package com.musicapp.backend.repository;

import com.musicapp.backend.dto.singer.SingerDto;
import com.musicapp.backend.entity.Singer;
import com.musicapp.backend.entity.Singer.SingerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SingerRepository extends JpaRepository<Singer, Long> {

    Optional<Singer> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Singer> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT COUNT(song) FROM Song song JOIN song.singers s WHERE s.id = :singerId")
    Long countSongsBySingerId(@Param("singerId") Long singerId);

    @Query("SELECT new com.musicapp.backend.dto.singer.SingerDto(s.id, s.name, s.email, s.avatarPath, COUNT(song.id), s.creator.id, s.creator.displayName, s.status) " +
            "FROM Singer s LEFT JOIN s.songs song " +
            "WHERE s.status = com.musicapp.backend.entity.Singer.SingerStatus.APPROVED " +
            "GROUP BY s.id, s.name, s.email, s.avatarPath, s.creator.id, s.creator.displayName, s.status " +
            "ORDER BY s.name ASC")
    Page<SingerDto> findAllWithSongCount(Pageable pageable);

    @Query("SELECT new com.musicapp.backend.dto.singer.SingerDto(s.id, s.name, s.email, s.avatarPath, COUNT(song.id), s.creator.id, s.creator.displayName, s.status) " +
            "FROM Singer s LEFT JOIN s.songs song " +
            "WHERE s.status = com.musicapp.backend.entity.Singer.SingerStatus.APPROVED AND LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "GROUP BY s.id, s.name, s.email, s.avatarPath, s.creator.id, s.creator.displayName, s.status " +
            "ORDER BY s.name ASC")
    Page<SingerDto> searchAllWithSongCount(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT s FROM Singer s WHERE (s.creator.id = :creatorId AND s.status = :status) OR s.creator.id IS NULL AND s.status = :status ORDER BY s.name ASC")
    List<Singer> findByCreatorIdAndStatusOrStatus(
            @Param("creatorId") Long creatorId,
            @Param("status") SingerStatus status
    );

    @Query("SELECT new com.musicapp.backend.dto.singer.SingerDto(s.id, s.name, s.email, s.avatarPath, COUNT(song.id), c.id, c.displayName, s.status) " +
            "FROM Singer s LEFT JOIN s.songs song LEFT JOIN s.creator c " +
            "WHERE (:status IS NULL OR s.status = :status) " +
            "GROUP BY s.id, s.name, s.email, s.avatarPath, c.id, c.displayName, s.status " +
            "ORDER BY s.id DESC")
    Page<SingerDto> findAllWithSongCountForAdmin(Pageable pageable, @Param("status") Singer.SingerStatus status);

    @Query("SELECT new com.musicapp.backend.dto.singer.SingerDto(s.id, s.name, s.email, s.avatarPath, COUNT(song.id), c.id, c.displayName, s.status) " +
            "FROM Singer s LEFT JOIN s.songs song LEFT JOIN s.creator c " +
            "WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "AND (:status IS NULL OR s.status = :status) " +
            "GROUP BY s.id, s.name, s.email, s.avatarPath, c.id, c.displayName, s.status " +
            "ORDER BY s.id DESC")
    Page<SingerDto> searchAllWithSongCountForAdmin(@Param("keyword") String keyword, Pageable pageable, @Param("status") Singer.SingerStatus status);

    @Query("SELECT s FROM Singer s ORDER BY s.name ASC")
    Page<Singer> findAllOrderByNameAsc(Pageable pageable);

    @Query("SELECT s FROM Singer s WHERE s.name LIKE %:keyword% ORDER BY s.name ASC")
    Page<Singer> findByNameContainingIgnoreCaseOrderByNameAsc(@Param("keyword") String keyword, Pageable pageable);

}