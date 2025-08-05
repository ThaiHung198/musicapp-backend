package com.musicapp.backend.repository;

import com.musicapp.backend.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    Optional<Tag> findByNameIgnoreCase(String name);
    
    boolean existsByNameIgnoreCase(String name);
    
    @Query("SELECT t FROM Tag t ORDER BY t.name ASC")
    List<Tag> findAllOrderByNameAsc();
    
    @Query("SELECT t FROM Tag t WHERE t.name LIKE %:keyword% ORDER BY t.name ASC")
    List<Tag> findByNameContainingIgnoreCaseOrderByNameAsc(String keyword);
}
