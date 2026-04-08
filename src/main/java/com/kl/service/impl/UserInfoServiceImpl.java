package com.kl.service.impl;

import com.kl.entity.UserInfo;
import com.kl.repository.UserInfoRepository;
import com.kl.service.IUserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class UserInfoServiceImpl  implements IUserInfoService {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Override
    public UserInfo getById(Long userId) {
        UserInfo userInfo = userInfoRepository.findById(userId).orElse(null);
        return userInfo;
    }
}
