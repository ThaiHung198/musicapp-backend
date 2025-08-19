package com.musicapp.backend.repository;

import com.musicapp.backend.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT t FROM Tag t ORDER BY t.name ASC")
    List<Tag> findAllByOrderByNameAsc();

    @Query(value = "SELECT t.id, t.name, COUNT(s.id) " +
            "FROM Tag t LEFT JOIN t.songs s " +
            "GROUP BY t.id, t.name",
            countQuery = "SELECT COUNT(t) FROM Tag t")
    Page<Object[]> findAllWithSongCount(Pageable pageable);

    @Query(value = "SELECT t.id, t.name, COUNT(s.id) " +
            "FROM Tag t LEFT JOIN t.songs s " +
            "WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "GROUP BY t.id, t.name",
            countQuery = "SELECT COUNT(t) FROM Tag t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Object[]> findByNameContainingWithSongCount(@Param("keyword") String keyword, Pageable pageable);

    List<Tag> findByNameIn(List<String> names);

    Optional<Tag> findByName(String name);
}