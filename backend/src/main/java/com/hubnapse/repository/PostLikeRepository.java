package com.hubnapse.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hubnapse.entity.PostLikeEntity;

public interface PostLikeRepository extends JpaRepository<PostLikeEntity, Long> {

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    void deleteByPostIdAndUserId(Long postId, Long userId);

    long countByPostId(Long postId);

    @Query("SELECT pl.post.id AS postId, COUNT(pl) AS likeCount FROM PostLikeEntity pl "
            + "WHERE pl.post.id IN :postIds GROUP BY pl.post.id")
    List<PostLikeCount> countGroupedByPostIds(@Param("postIds") Collection<Long> postIds);

    @Query("SELECT pl.post.id FROM PostLikeEntity pl WHERE pl.user.id = :userId AND pl.post.id IN :postIds")
    List<Long> findLikedPostIds(@Param("userId") Long userId, @Param("postIds") Collection<Long> postIds);

    interface PostLikeCount {
        Long getPostId();

        Long getLikeCount();
    }
}
