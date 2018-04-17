package com.wxm.service;

import com.wxm.model.OAUser;

import java.util.Map;

public interface UserService {
     Map getUserList(Integer offset, Integer limit, String userName);
     OAUser getUserById(Integer id);
     OAUser selectByName(String userName);
     Integer create(OAUser oaUser);
     Integer update(OAUser oaUser);
}
