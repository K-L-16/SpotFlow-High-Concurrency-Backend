package com.kl.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kl.dto.LoginFormDTO;
import com.kl.dto.Result;
import com.kl.dto.UserDTO;
import com.kl.entity.User;

import com.kl.properties.JwtProperties;
import com.kl.repository.UserRepository;
import com.kl.service.IUserService;
import com.kl.utils.JwtUtils;
import com.kl.utils.RegexUtils;
import com.kl.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.kl.utils.RedisConstants.*;
import static com.kl.utils.SystemConstants.USER_NICK_NAME_PREFIX;


@Service
@Slf4j
public class UserServiceImpl  implements IUserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 发送手机验证码（并未实现真实发送手机验证码，而是在本地console模拟）
     * @param phone
     * @return
     */
    @Override
    public Result sendCode(String phone) {
        // 1. 校验手机号
        if(RegexUtils.isPhoneInvalid(phone)){
            //2. 如果不符合，返回错误信息
            return Result.fail("手机号格式无效");
        }
        //3. 符合，生成验证码
        String code = String.valueOf((int)((Math.random() * 9 + 1) * 100000));
        //4.保存验证码到redis 并设置过期时间
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY +phone,code,LOGIN_CODE_TTL, TimeUnit.MINUTES);
        //5. 发送验证码
        log.debug("发送短信验证码成功，验证码：{}", code);
        //返回ok
        return Result.ok();
    }

    /**
     * 登录功能
     * @param loginForm
     * @param
     * @return
     */
    @Override
    public Result login(LoginFormDTO loginForm) {
        // 1. 校验手机号
        if (RegexUtils.isPhoneInvalid(loginForm.getPhone())) {
            return Result.fail("手机号格式无效");
        }

        // 2. 从 Redis 校验验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + loginForm.getPhone());
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            return Result.fail("验证码错误");
        }

        // 3. 根据手机号查询用户
        User user = userRepository.findByPhone(loginForm.getPhone()).orElse(null);

        // 4. 用户不存在则创建
        if (user == null) {
            user = createUserWithPhone(loginForm.getPhone());
        }

        // 5. 生成 JWT
        String token = JwtUtils.generateToken(user.getId(), jwtProperties.getSecret(), jwtProperties.getExpirationMillis());

        // 6. 返回 JWT
        return Result.ok(token);
    }


    /**
     * 创建用户
     * @param phone
     * @return
     */
    private User createUserWithPhone(String phone) {
        //创建用户
        User user = new User();
        user.setPhone(phone);
        String nickName = USER_NICK_NAME_PREFIX + UUID.randomUUID().toString().substring(0, 10);
        user.setNickName(nickName);
        //保存用户
        userRepository.save(user);
        return user;
    }

    /**
     * 签到功能
     * @return
     */
    @Override
    public Result sign() {
        //1.获取当前登录的用户
        Long userId = UserHolder.getUser().getId();
        //2.获取日期
        LocalDateTime now = LocalDateTime.now();
        //3.拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = USER_SIGN_KEY + userId + keySuffix;
        //4.获取今天是本月的第几天
        int dayOfMonth = now.getDayOfMonth();
        //5.写入redis setbit key offset 1
        stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
        return Result.ok();
    }

    /**
     * 获取签到次数
     * @return
     */
    @Override
    public Result signCount() {
        //1.获取当前登录的用户
        Long userId = UserHolder.getUser().getId();
        //2.获取日期
        LocalDateTime now = LocalDateTime.now();
        //3.拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = USER_SIGN_KEY + userId + keySuffix;
        //4.获取今天是本月的第几天
        int dayOfMonth = now.getDayOfMonth();
        //5.获取本月截至今天为止的所有的签到记录，返回的是一个十进制的数字
        List<Long> result = stringRedisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0)
        );
        if (result == null || result.size() == 0){
            //没有任何签到结果
            return Result.ok(0);
        }
        Long num = result.get(0);
        if (num== null || num == 0){
            return Result.ok(0);
        }
        //6.循环遍历
        int count = 0;
        while(true){
            //7.让这个数字与1做与运算，得到的数字的左右一个bit位
            //8.判断这个bit位是否为0
            if((num & 1) == 0){
                //9.如果为0，说明未签到，结束
                break;
            }else{
                //10.如果为1，说明已签到，计数器+1
                count++;
            }
            //把数字右移一位，抱起最后一个bit位，继续下一个bit位
             num >>>= 1;
        }
        return Result.ok(count);

    }

    //前端携带着authorization: jwt
    /**
     * 退出登录功能
     * @param authHeader
     * @return
     */
    @Override
    public Result logout(String authHeader) {
        if (authHeader == null || StringUtils.isBlank(authHeader)) {
            return Result.ok();
        }

        String token = authHeader;

        try {
            Date expiration = JwtUtils.getExpiration(token, jwtProperties.getSecret());
            //检查jwt是否过期
            long ttl = expiration.getTime() - System.currentTimeMillis();
            //设置jwt过期黑名单
            if (ttl > 0) {
                stringRedisTemplate.opsForValue().set(
                        "jwt:blacklist:" + token,
                        "1",
                        ttl,
                        TimeUnit.MILLISECONDS
                );
            }
        } catch (Exception e) {
            // token 无效也可以直接认为“已经退出”
            return Result.fail("退出失败");
        }

        return Result.ok();
    }

    /**
     * 根据id查询用户
     * @param id
     * @return
     */
    @Override
    public User findById(Long id) {
        User user = userRepository.findById(id).orElse(null);
        return user;
    }

    @Override
    public List<User> findAllById(Iterable<Long> ids) {
        List<User> users = new ArrayList<>();
        userRepository.findAllById(ids).forEach(users::add);
        return users;
    }
}
