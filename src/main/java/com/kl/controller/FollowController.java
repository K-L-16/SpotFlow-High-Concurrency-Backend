package com.kl.controller;


import com.kl.dto.Result;
import com.kl.service.IFollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/follow")
@Tag(name = "Follow api")
public class FollowController {

    @Autowired
    private IFollowService followService;

    /**
     * 关注或取消关注
     * @param followUserId
     * @param isFollow
     * @return
     */
    @Operation(summary = "follow user")
    @PutMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable("id") Long followUserId, @PathVariable("isFollow") Boolean isFollow) {
        return followService.follow(followUserId, isFollow);
    }

    /**
     * 判断当前登录用户是否关注了指定用户
     * @param followUserId
     * @return
     */
    @Operation(summary = "check if is follow")
    @GetMapping("/or/not/{id}")
    public Result isFollow(@PathVariable("id") Long followUserId) {
        return followService.isFollow(followUserId);
    }

    /**
     * 获取当前登录用户关注的用户
     * @param id
     * @return
     */
    @Operation(summary = "get follow user")
    @GetMapping("/common/{id}")
    public Result followCommons(@PathVariable("id") Long id) {
        return followService.followCommons(id);
    }
}
