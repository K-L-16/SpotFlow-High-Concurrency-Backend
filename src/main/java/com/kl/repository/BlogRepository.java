package com.kl.repository;

import com.kl.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface BlogRepository extends JpaRepository<Blog,Long> {
    /**
     * 点赞+1
     * @param id
     * @return
     */
    @Modifying
    @Query("update Blog b set b.liked = b.liked + 1 where b.id = :id")
    int incrementLiked(@Param("id") Long id);

    /**
     * 点赞-1
     * @param id
     * @return
     */
    @Modifying
    @Query("update Blog b set b.liked = b.liked - 1 where b.id = :id")
    int decrementLiked(@Param("id") Long id);

    Page<Blog> findByUserId(Long userId, Pageable pageable);
}
