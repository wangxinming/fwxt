package com.wxm.mapper;

import com.wxm.model.OAGroup;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface OAGroupMapper {
    int deleteByPrimaryKey(Integer groupId);

    int insert(OAGroup record);

    int insertSelective(OAGroup record);

    OAGroup selectByPrimaryKey(Integer groupId);

    int updateByPrimaryKeySelective(OAGroup record);

    int updateByPrimaryKey(OAGroup record);

    OAGroup getGroupByUserId(Integer userId);

    List<OAGroup> list(@Param("groupName") String groupName, @Param("offset") Integer offset, @Param("limit")  Integer limit, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    Integer count(@Param("groupName") String groupName, @Param("startTime") Date startTime,@Param("endTime") Date endTime);

    List<OAGroup> total();
}