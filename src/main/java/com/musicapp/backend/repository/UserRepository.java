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

    @Query(value = "SELECT u.* FROM users u " +
            "JOIN user_roles ur ON u.id = ur.user_id " +
            "JOIN roles r ON ur.role_id = r.id " +
            "WHERE r.name = :roleName AND " +
            "(LOWER(COALESCE(u.display_name, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))",
            countQuery = "SELECT COUNT(DISTINCT u.id) FROM users u " +
                    "JOIN user_roles ur ON u.id = ur.user_id " +
                    "JOIN roles r ON ur.role_id = r.id " +
                    "WHERE r.name = :roleName AND " +
                    "(LOWER(COALESCE(u.display_name, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                    "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))",
            nativeQuery = true)
    Page<User> findByRoleNameAndKeyword(@Param("roleName") String roleName, @Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT u.* FROM users u " +
            "JOIN user_roles ur ON u.id = ur.user_id " +
            "JOIN roles r ON ur.role_id = r.id " +
            "WHERE (LOWER(COALESCE(u.display_name, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "GROUP BY u.id, u.display_name, u.email, u.password, u.phone_number, u.avatar_path, u.date_of_birth, u.gender, u.provider, u.status, u.created_at, u.updated_at, u.otp_code, u.otp_expiration_time " +
            "HAVING COUNT(ur.role_id) = 1 AND MAX(r.name) = 'ROLE_USER'",
            countQuery = "SELECT COUNT(*) FROM (" +
                    "SELECT u.id FROM users u " +
                    "JOIN user_roles ur ON u.id = ur.user_id " +
                    "JOIN roles r ON ur.role_id = r.id " +
                    "WHERE (LOWER(COALESCE(u.display_name, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                    "GROUP BY u.id " +
                    "HAVING COUNT(ur.role_id) = 1 AND MAX(r.name) = 'ROLE_USER'" +
                    ") AS user_count",
            nativeQuery = true)
    Page<User> findAppUsers(@Param("keyword") String keyword, Pageable pageable);


    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
}