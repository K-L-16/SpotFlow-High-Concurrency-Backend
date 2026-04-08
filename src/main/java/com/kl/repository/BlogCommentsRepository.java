package com.kl.repository;

import com.kl.entity.BlogComments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogCommentsRepository extends JpaRepository<BlogComments,Long> {
}
