package com.hubnapse.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hubnapse.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<UserEntity> findByEmail(String email);
}
