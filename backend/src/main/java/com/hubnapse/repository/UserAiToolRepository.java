package com.hubnapse.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hubnapse.entity.UserAiToolEntity;

public interface UserAiToolRepository extends JpaRepository<UserAiToolEntity, Long> {

    @EntityGraph(attributePaths = "aiTool")
    List<UserAiToolEntity> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
