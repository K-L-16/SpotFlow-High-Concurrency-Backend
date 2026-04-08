package com.kl.service;

import com.kl.dto.LoginFormDTO;
import com.kl.dto.Result;
import com.kl.entity.User;

import jakarta.servlet.http.HttpSession;

public interface IUserService {

    Result sendCode(String phone);

    Result login(LoginFormDTO loginForm);

    Result sign();

    Result signCount();

    Result logout(String token);

    User findById(Long id);
}