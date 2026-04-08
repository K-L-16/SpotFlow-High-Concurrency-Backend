package com.kl.service;


import com.kl.dto.Result;
import com.kl.entity.Follow;

import java.util.List;

public interface IFollowService {

    Result follow(Long followUserId, Boolean isFollow);

    Result isFollow(Long followUserId);

    Result followCommons(Long id);

    List<Follow> queryByFollowUserId(Long followUserId);
}
