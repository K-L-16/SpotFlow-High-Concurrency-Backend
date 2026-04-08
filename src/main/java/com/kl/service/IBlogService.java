package com.kl.service;

import com.kl.dto.Result;
import com.kl.entity.Blog;
import org.springframework.data.domain.Page;

public interface IBlogService {

    Result queryHotBlog(Integer current);

    Result queryBlogById(Long id);

    Result likeBlog(Long id);

    Result queryBlogLikes(Long id);

    Result saveBlog(Blog blog);

    Result queryBlogOfFollow(Long max, Integer offset);

    Page<Blog> queryBlogByUserId(Long userId, Integer current);
}
