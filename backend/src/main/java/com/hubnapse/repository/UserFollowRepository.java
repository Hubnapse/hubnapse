package com.hubnapse.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hubnapse.entity.UserFollowEntity;

public interface UserFollowRepository extends JpaRepository<UserFollowEntity, Long> {

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);

    long countByFollowingId(Long followingId);

    long countByFollowerId(Long followerId);

    @Query("SELECT uf.following.id FROM UserFollowEntity uf "
            + "WHERE uf.follower.id = :followerId AND uf.following.id IN :candidateIds")
    List<Long> findFollowingIdsAmong(@Param("followerId") Long followerId, @Param("candidateIds") Collection<Long> candidateIds);
}
