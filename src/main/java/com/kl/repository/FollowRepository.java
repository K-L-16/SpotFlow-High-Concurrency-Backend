package com.kl.repository;

import com.kl.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByUserIdAndFollowUserId(Long userId, Long followUserId);

    @Transactional
    long deleteByUserIdAndFollowUserId(Long userId, Long followUserId);

    List<Follow> findByFollowUserId(Long followUserId);
}
