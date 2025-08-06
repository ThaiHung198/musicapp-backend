package com.musicapp.backend.repository;

import com.musicapp.backend.dto.singer.SingerDto;
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

    Optional<Singer> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Singer> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT COUNT(song) FROM Song song JOIN song.singers s WHERE s.id = :singerId")
    Long countSongsBySingerId(@Param("singerId") Long singerId);

    // --- THÊM MỚI: Phương thức tối ưu để lấy tất cả ca sĩ ---
    @Query("SELECT new com.musicapp.backend.dto.singer.SingerDto(s.id, s.name, s.email, s.avatarPath, COUNT(song.id), s.creator.id, s.creator.displayName) " +
            "FROM Singer s LEFT JOIN s.songs song " +
            "GROUP BY s.id, s.name, s.email, s.avatarPath, s.creator.id, s.creator.displayName " +
            "ORDER BY s.name ASC")
    Page<SingerDto> findAllWithSongCount(Pageable pageable);

    // --- THÊM MỚI: Phương thức tối ưu để tìm kiếm ca sĩ ---
    @Query("SELECT new com.musicapp.backend.dto.singer.SingerDto(s.id, s.name, s.email, s.avatarPath, COUNT(song.id), s.creator.id, s.creator.displayName) " +
            "FROM Singer s LEFT JOIN s.songs song " +
            "WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "GROUP BY s.id, s.name, s.email, s.avatarPath, s.creator.id, s.creator.displayName " +
            "ORDER BY s.name ASC")
    Page<SingerDto> searchAllWithSongCount(@Param("keyword") String keyword, Pageable pageable);

    // Các phương thức cũ không dùng cho việc lấy danh sách nữa nhưng có thể vẫn cần ở nơi khác
    @Query("SELECT s FROM Singer s ORDER BY s.name ASC")
    Page<Singer> findAllOrderByNameAsc(Pageable pageable);

    @Query("SELECT s FROM Singer s WHERE s.name LIKE %:keyword% ORDER BY s.name ASC")
    Page<Singer> findByNameContainingIgnoreCaseOrderByNameAsc(@Param("keyword") String keyword, Pageable pageable);
}