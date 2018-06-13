package com.wxm.service.impl;

import com.alibaba.druid.support.json.JSONUtils;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.wxm.mapper.OAGroupMapper;
import com.wxm.model.OAGroup;
import com.wxm.service.GroupService;
import com.wxm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GroupServiceImpl implements GroupService{
    @Autowired
    private OAGroupMapper oaGroupMapper;
    @Override
    public Map<String, Boolean> getBarsByUserId(Integer userId) {
//        userService.getUserById()
        OAGroup oaGroup = oaGroupMapper.getGroupByUserId(userId);
        Map<String,Object> map = (Map<String,Object>)JSONUtils.parse( oaGroup.getPrivilegeids());
        return null;
    }

    @Override
    public Integer saveGroup(String groupName,Integer userId,String privilegeids,String describe,Integer status) {
        OAGroup group = new OAGroup(groupName,userId,privilegeids,describe,status);
        return oaGroupMapper.insertSelective(group);
    }

    @Override
    public OAGroup getGroupById(Integer groupId) {
        return oaGroupMapper.selectByPrimaryKey(groupId);
    }
}
