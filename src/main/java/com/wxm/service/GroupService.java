package com.wxm.service;

import com.wxm.model.OAGroup;

import java.util.Map;

public interface GroupService {
    Map<String,Boolean> getBarsByUserId(Integer userId);
    Integer saveGroup(String groupName,Integer userId,String privilegeids,String describe,Integer status);
    OAGroup getGroupById(Integer groupId);

}
