package com.wxm.service;

import com.wxm.model.OAUser;

import java.util.List;
import java.util.Map;

public interface UserService {
     Map getUserList(Integer offset, Integer limit, String userName);
     OAUser getUserById(Integer id);
     List<OAUser> getAllUser();
     OAUser selectByName(String userName);
     Integer create(OAUser oaUser);
     Integer update(OAUser oaUser);
     Integer updateStatus(OAUser oaUser);
     Integer updatePsw(OAUser oaUser);
     OAUser getLeader(OAUser oaUser);
}
