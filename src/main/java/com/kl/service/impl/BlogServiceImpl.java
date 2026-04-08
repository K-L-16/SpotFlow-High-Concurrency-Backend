package com.kl.service.impl;


import com.kl.dto.Result;
import com.kl.dto.ScrollResult;
import com.kl.dto.UserDTO;
import com.kl.entity.Blog;
import com.kl.entity.Follow;
import com.kl.entity.User;
import com.kl.repository.BlogRepository;
import com.kl.service.IBlogService;
import com.kl.service.IFollowService;
import com.kl.service.IUserService;
import com.kl.utils.SystemConstants;
import com.kl.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.kl.utils.RedisConstants.BLOG_LIKED_KEY;
import static com.kl.utils.RedisConstants.FEED_KEY;


@Service
public class BlogServiceImpl  implements IBlogService {

    @Autowired
    private IUserService userService;

    @Autowired
    private IFollowService followService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private BlogRepository blogRepository;

    /**
     * 查询笔记（首页更新）
     * @param current
     * @return
     */
    @Override
    public Result queryHotBlog(Integer current) {
        // 根据用户查询
        Pageable pageable = PageRequest.of(
                current - 1,
                SystemConstants.MAX_PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "liked")
        );

        Page<Blog> page = blogRepository.findAll(pageable);
        // 获取当前页数据
        List<Blog> records = page.getContent();
        // 查询用户
        records.forEach(blog ->{
            Long userId = blog.getUserId();
            User user = userService.findById(userId);
            blog.setName(user.getNickName());
            blog.setIcon(user.getIcon());
            //添加博客是否被点赞
            isBlogLiked(blog);
        });
        return Result.ok(records);
    }

    /**
     * 查询笔记详情
     * @param id
     * @return
     */
    @Override
    public Result queryBlogById(Long id) {
        //查询blog
        Blog blog = blogRepository.findById(id).orElse(null);
        if (blog == null){
            return Result.fail("笔记不存在");
        }
        //2. 查询blog有关的用户
        Long userId = blog.getUserId();
        User user = userService.findById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
        //查询blog是否被点赞了
        isBlogLiked(blog);
        return Result.ok(blog);
    }

    /**
     * 判断当前登录用户是否已经点赞
     * @param blog
     */
    private void isBlogLiked(Blog blog) {
        // 1. 获取当前登录用户
        if (UserHolder.getUser() == null) {
            blog.setIsLike(false);
            return;
        }

        Long userId = UserHolder.getUser().getId();

        // 2. 判断当前登录用户是否已经点赞
        String key = "blog:liked:" + blog.getId();
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        blog.setIsLike(score != null);
    }

    /**
     * 点赞
     * @param id
     * @return
     */
    @Override
    @Transactional
    public Result likeBlog(Long id) {
        //1.获取登录用户
        Long userId = UserHolder.getUser().getId();
        //2.判单当前登录的用户是否已经点赞
        String key = BLOG_LIKED_KEY + id;
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        if (score == null){
            //3.如果未点赞，可以点赞
            //3.1数据库点赞数+1
            int rows = blogRepository.incrementLiked(id);
            boolean isSuccess = rows > 0;
            //3.2保存用户到redis的set集合 zadd key value score
            if (isSuccess){
                stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
            }
        }else{
            //3.1如果已经点赞，取消点赞
            //3.1.1数据库点赞数-1
            int row = blogRepository.decrementLiked( id);
            boolean isSuccess = row > 0;
            //3.1.2redis集合remove
            if (isSuccess){
                stringRedisTemplate.opsForZSet().remove(key, userId.toString());
            }
        }

        return Result.ok();
    }

    @Override
    public Result queryBlogLikes(Long id) {
        //1.查询top5的点赞用户 zrange key 0 4
        String key = BLOG_LIKED_KEY + id;
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        if (top5 == null || top5.isEmpty()){
            return Result.ok(Collections.emptyList());
        }
        //2.解析出其中的用户id
        List<Long> userIds = top5.stream().map(Long::valueOf).collect(Collectors.toList());
        //3.根据用户id查询用户
        List<User> users = userService.findAllById(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        List<UserDTO> userDTOs = userIds.stream()
                .map(userMap::get)
                .filter(Objects::nonNull)
                .map(user -> {
                    UserDTO dto = new UserDTO();
                    dto.setId(user.getId());
                    dto.setNickName(user.getNickName());
                    dto.setIcon(user.getIcon());
                    return dto;
                })
                .collect(Collectors.toList());
        //4.返回
        return Result.ok(userDTOs);
    }

    @Override
    public Result saveBlog(Blog blog) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());
        // 保存探店博文
        blogRepository.save(blog);
        //查询笔记作者的所有粉丝
        List<Follow> follows = followService.queryByFollowUserId(user.getId());
        //推送笔记id给所有粉丝
        for (Follow follow : follows) {
            //4.1获得粉丝id
            Long userId = follow.getUserId();
            //4.2推送
            String key = "feed:" + userId;
            stringRedisTemplate.opsForZSet().add(key, blog.getId().toString(), System.currentTimeMillis());

        }

        // 返回id
        return Result.ok(blog.getId());
    }

    /**
     * 查询关注者blog
     * @param max
     * @param offset
     * @return
     */
    @Override
    public Result queryBlogOfFollow(Long max, Integer offset) {
        //1.获取当前用户
        Long userId = UserHolder.getUser().getId();
        //2.查询收件箱 zrevrange key max min limit offset
        String key = FEED_KEY + userId;
        Set<ZSetOperations.TypedTuple<String>> typedValues = stringRedisTemplate
                .opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, max, offset, 2);

        //3.判断非空
        if (typedValues == null || typedValues.isEmpty()){
            return Result.ok();
        }
        //4.解析数据
        List<Long> ids = new ArrayList<>(typedValues.size());
        long minTime = 0;
        int os = 1;
        for (ZSetOperations.TypedTuple<String> typedValue : typedValues){
            //4.1获取id
            String blogId = typedValue.getValue();
            ids.add(Long.valueOf(blogId));
            //4.2获取分数
            long time = typedValue.getScore().longValue();
            if (time == minTime){
                os++;
            }else{
                minTime = time;
                os = 1;
            }
        }
        //4.根据id查询blog
        List<Blog> blogs = blogRepository.findAllById(ids);

        for (Blog blog : blogs) {
            Long userId1 = blog.getUserId();
            User user = userService.findById(userId1);
            blog.setName(user.getNickName());
            blog.setIcon(user.getIcon());
            //添加博客是否被点赞
            isBlogLiked(blog);
        }
        //5.封装并返回
        ScrollResult scrollResult = new ScrollResult();
        scrollResult.setList(blogs);
        scrollResult.setOffset(os);
        scrollResult.setMinTime(minTime);
        return Result.ok(scrollResult);

    }

    @Override
    public Page<Blog> queryBlogByUserId(Long userId, Integer current) {
        Pageable pageable = PageRequest.of(
                current - 1,
                SystemConstants.MAX_PAGE_SIZE
        );
        return blogRepository.findByUserId(userId, pageable);
    }
}
