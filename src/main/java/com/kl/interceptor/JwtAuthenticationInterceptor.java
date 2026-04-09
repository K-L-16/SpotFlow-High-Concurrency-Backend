package com.kl.interceptor;


import com.kl.dto.UserDTO;
import com.kl.entity.User;
import com.kl.service.IUserService;
import com.kl.utils.JwtUtils;
import com.kl.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


//当前方法是利用jwt和redis进行拦截，查看redis里面的黑名单，如果黑名单有，则返回401
@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    @Autowired
    private IUserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //前端请求头还是传递authorization: jwt
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("authorization");
        if (authHeader == null || StringUtils.isBlank(authHeader)) {
            return true; // 先放行，后面由 LoginInterceptor 判断是否需要登录
        }

        String token = authHeader;

        try {
            // 1. 黑名单校验
            Boolean exists = stringRedisTemplate.hasKey("jwt:blacklist:" + token);
            if (Boolean.TRUE.equals(exists)) {
                // 公开接口允许匿名访问，黑名单 token 按未登录处理
                UserHolder.removeUser();
                return true;
            }

            // 2. 解析 JWT
            Long userId = JwtUtils.getUserId(token);

            // 3. 查用户
            User user = userService.findById(userId);
            if (user == null) {
                UserHolder.removeUser();
                return true;
            }

            // 4. 转 UserDTO，保存到 ThreadLocal
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);
            UserHolder.saveUser(userDTO);

        } catch (Exception e) {
            // token 过期或非法时，按匿名用户处理，避免影响公开接口联调
            UserHolder.removeUser();
            return true;
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
