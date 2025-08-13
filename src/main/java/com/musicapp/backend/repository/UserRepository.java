package com.musicapp.backend.repository;

import com.musicapp.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE " +
            "EXISTS (SELECT r FROM u.roles r WHERE r.name = 'ROLE_USER') AND " +
            "NOT EXISTS (SELECT r FROM u.roles r WHERE r.name IN ('ROLE_ADMIN', 'ROLE_CREATOR')) AND " +
            "(LOWER(u.displayName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> findAppUsers(@Param("keyword") String keyword, Pageable pageable);

    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
}