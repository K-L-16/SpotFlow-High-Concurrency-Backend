package com.kl.service;

import com.kl.entity.UserInfo;

public interface IUserInfoService {
    UserInfo getById(Long userId);
}
