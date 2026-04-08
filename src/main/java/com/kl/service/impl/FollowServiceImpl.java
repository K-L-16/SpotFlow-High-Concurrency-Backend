package com.kl.service.impl;

import com.kl.dto.Result;
import com.kl.dto.UserDTO;
import com.kl.entity.Follow;
import com.kl.repository.FollowRepository;
import com.kl.service.IFollowService;
import com.kl.service.IUserService;
import com.kl.utils.UserHolder;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class FollowServiceImpl implements IFollowService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private IUserService userService;

    @Autowired
    private FollowRepository followRepository;

    @Override
    @Transactional
    public Result follow(Long followUserId, Boolean isFollow) {
        // 1. 获取当前用户
        Long userId = UserHolder.getUser().getId();
        String key = "follows:" + userId;

        if (Boolean.TRUE.equals(isFollow)) {
            // 2. 关注：保存数据库记录
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);

            try {
                followRepository.save(follow);
                // 3. 数据库成功，再写 Redis
                stringRedisTemplate.opsForSet().add(key, followUserId.toString());
            } catch (Exception e) {
                return Result.fail("关注失败");
            }

        } else {
            // 4. 取关：删除数据库记录
            long rows = followRepository.deleteByUserIdAndFollowUserId(userId, followUserId);

            if (rows > 0) {
                // 5. 数据库删除成功，再删 Redis
                stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
            }
        }

        return Result.ok();
    }

    @Override
    public Result isFollow(Long followUserId) {
        //1.查询是否关注select * from tb_follow where user_id = ? and follow_user_id = ?
        Long userId = UserHolder.getUser().getId();
        boolean isFollow = followRepository.existsByUserIdAndFollowUserId(userId, followUserId);
        return Result.ok(isFollow);
    }

    @Override
    public Result followCommons(Long id) {
        //1.获取当前用户
        Long userId = UserHolder.getUser().getId();
        String key = "follows:"+userId;
        String key2 = "follows:"+id;
        //2.求交集
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(key, key2);
        //3.解析id
        if (intersect == null || intersect.isEmpty()){
            //无交集
            return Result.ok(Collections.emptyList());
        }
        List<Long> ids = intersect.stream().map(Long::valueOf).collect(Collectors.toList());
        //4.查询用户
        //todo这里用mybatis
        List<UserDTO> userDTOs = userService.findAllById(ids)
                .stream()
                .map(user -> {
                    UserDTO dto = new UserDTO();
                    BeanUtils.copyProperties(user, dto);
                    return dto;
                })
                .collect(Collectors.toList());

        return Result.ok(userDTOs);
    }

    @Override
    public List<Follow> queryByFollowUserId(Long followUserId) {
        return followRepository.findByFollowUserId(followUserId);
    }
}
