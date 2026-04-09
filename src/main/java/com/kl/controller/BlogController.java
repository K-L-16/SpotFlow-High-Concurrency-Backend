package com.kl.controller;


import com.kl.dto.Result;
import com.kl.dto.UserDTO;
import com.kl.entity.Blog;
import com.kl.service.IBlogService;
import com.kl.utils.SystemConstants;
import com.kl.utils.UserHolder;
import com.kl.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/blog")
@Tag(name = "Blog api")
public class BlogController {

    @Autowired
    private IBlogService blogService;

    /**
     * 查询博文by用户id
     * @param current
     * @param id
     * @return
     */
    @Operation(summary = "Search user by userID")
    @GetMapping("/of/user")
    public Result queryBlogByUserId(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam("id") Long id) {
        // 根据用户查询
        Page<Blog> page = blogService.queryBlogByUserId(id, current);
        List<Blog> records = page.getContent();
        return Result.ok(records);
    }


    /**
     * 保存博文
     * @param blog
     * @return
     */
    @Operation(summary = "Save blog")
    @PostMapping
    public Result saveBlog(@RequestBody Blog blog) {
        return blogService.saveBlog(blog);
    }

    /**
     * 点赞
     * @param id
     * @return
     */
    @Operation(summary = "Like blog")
    @PutMapping("/like/{id}")
    public Result likeBlog(@PathVariable("id") Long id) {
        // 修改点赞数量
        return blogService.likeBlog(id);
    }

    /**
     * 查询当前登录用户的所有的笔记
     * @return
     */
    @Operation(summary = "Query my blog")
    @GetMapping("/of/me")
    public Result queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();

        // 根据用户查询
        Page<Blog> page = blogService.queryBlogByUserId(user.getId(), current);
        List<Blog> records = page.getContent();
        return Result.ok(records);
    }

    /**
     * 查询热门笔记
     * @param current
     * @return
     */
    @Operation(summary = "Query hot blog")
    @GetMapping("/hot")
    public Result queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return blogService.queryHotBlog(current);
    }

    /**
     * 查询笔记详情
     * @param id
     * @return
     */
    @Operation(summary = "Query blog detail")
    @GetMapping("/{id}")
    public Result queryBlogById(@PathVariable("id") Long id) {
        return blogService.queryBlogById(id);
    }

    /**
     * 查询笔记的点赞用户
     * @param id
     * @return
     */
    @Operation(summary = "Query blog likes")
    @GetMapping("/likes/{id}")
    public Result queryBlogLikes(@PathVariable("id") Long id) {
        return blogService.queryBlogLikes(id);
    }

    /**
     * 查询关注用户的博客流
     * @param max
     * @param offset
     * @return
     */
    @Operation(summary = "Query blog of follow")
    @GetMapping("/of/follow")
    public Result queryBlogOfFollow(
            @RequestParam("lastId") Long max, @RequestParam(value = "offset", defaultValue = "0") Integer offset
    ) {
        return blogService.queryBlogOfFollow(max, offset);
    }
}
