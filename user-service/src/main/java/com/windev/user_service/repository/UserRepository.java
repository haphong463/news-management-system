package com.windev.user_service.repository;

import com.windev.user_service.entity.Role;
import com.windev.user_service.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    @Override
    Page<User> findAll(Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r IN :roles")
    Page<User> findByRoles(@Param("roles") Set<Role> roles, Pageable pageable);
}