package com.wxm.service;

import com.wxm.entity.User;

public interface LoginService {
    /**
     * 用户登录
     * @param username 用户名
     * @return 登录用户
     */
    public User loginUser(String username)throws Exception;
}
