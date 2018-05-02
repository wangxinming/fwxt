package com.wxm.mapper;

import com.wxm.model.OAUser;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface OAUserMapper {
    int deleteByPrimaryKey(Integer userId);

    int insert(OAUser record);

    int insertSelective(OAUser record);

    OAUser selectByPrimaryKey(Integer userId);

    OAUser selectByName(String userName);

    int updateByPrimaryKeySelective(OAUser record);

    int updateByPrimaryKey(OAUser record);

    List<OAUser> list(@Param("offset") Integer offset, @Param("limit") Integer limit, @Param("userName") String userName);

    int count(@Param("userName") String userName);

    OAUser getLeader(OAUser record);

    List<OAUser> getAllUser();
}