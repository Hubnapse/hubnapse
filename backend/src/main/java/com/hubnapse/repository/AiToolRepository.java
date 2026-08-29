package com.hubnapse.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hubnapse.entity.AiToolEntity;

public interface AiToolRepository extends JpaRepository<AiToolEntity, Long> {

    boolean existsByName(String name);

    List<AiToolEntity> findAllByOrderByIdAsc();
}
