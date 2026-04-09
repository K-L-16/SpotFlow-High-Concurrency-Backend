package com.kl.controller;


import com.kl.dto.LoginFormDTO;
import com.kl.dto.Result;
import com.kl.dto.UserDTO;
import com.kl.entity.User;
import com.kl.entity.UserInfo;
import com.kl.service.IUserInfoService;
import com.kl.service.IUserService;
import com.kl.utils.UserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;




@Slf4j
@RestController
@RequestMapping("/user")
@Tag(name = "User api")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IUserInfoService userInfoService;


    /**
     * 根据id查询用户
     * @param userId
     * @return
     */
    @Operation(summary = "query user by id")
    @GetMapping("/{id}")
    public Result queryUserById(@PathVariable("id") Long userId){
        // 查询详情
        User user = userService.findById(userId);
        if (user == null) {
            return Result.ok();
        }
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        // 返回
        return Result.ok(userDTO);
    }

    /**
     * 发送手机验证码
     */
    @Operation(summary = "send code")
    @PostMapping("code")
    public Result sendCode(@RequestParam("phone") String phone) {
        // 发送短信验证码并保存验证码
        return userService.sendCode(phone);
    }

    /**
     * 登录功能
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     */
    @Operation(summary = "login")
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm){
        // 实现登录功能
        return userService.login(loginForm);
    }

    /**
     * 登出功能
     * @return 无
     */
    @Operation(summary = "logout")
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        String token = request.getHeader("authorization");
        return userService.logout(token);
    }

    /**
     * 获取当前登录用户并返回
     * @return
     */
    @Operation(summary = "get current login user")
    @GetMapping("/me")
    public Result me(){
        // 获取当前登录的用户并返回
        return Result.ok(UserHolder.getUser());
    }

    /**
     * 获取当前登录用户的信息
     * @param userId
     * @return
     */
    @Operation(summary = "get current login user detail info")
    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long userId){
        // 查询详情
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            // 没有详情，应该是第一次查看详情
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 返回
        return Result.ok(info);
    }

    /**
     * 签到功能
     * @return
     */
    @Operation(summary = "sign")
    @PostMapping("/sign")
    public Result sign(){
        return userService.sign();
    }

    /**
     * 统计签到功能
     * @return
     */
    @Operation(summary = "sign count")
    @GetMapping("/sign/count")
    public Result signCount(){
        return userService.signCount();
    }

}
