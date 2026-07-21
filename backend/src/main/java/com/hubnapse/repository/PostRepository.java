package com.hubnapse.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hubnapse.entity.PostEntity;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
}