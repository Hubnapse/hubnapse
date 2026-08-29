package com.hubnapse.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hubnapse.entity.PostEntity;

public interface PostRepository extends JpaRepository<PostEntity, Long> {

    @Override
    @EntityGraph(attributePaths = "author")
    List<PostEntity> findAll();

    @Override
    @EntityGraph(attributePaths = "author")
    Optional<PostEntity> findById(Long id);
}
