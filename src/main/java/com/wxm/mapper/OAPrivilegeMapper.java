package com.wxm.mapper;

import com.wxm.model.OAPrivilege;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OAPrivilegeMapper {
    int deleteByPrimaryKey(Integer privilegeId);

    int insert(OAPrivilege record);

    int insertSelective(OAPrivilege record);

    OAPrivilege selectByPrimaryKey(Integer privilegeId);

    int updateByPrimaryKeySelective(OAPrivilege record);

    int updateByPrimaryKey(OAPrivilege record);

    List<OAPrivilege> list(@Param("type")String type);
}