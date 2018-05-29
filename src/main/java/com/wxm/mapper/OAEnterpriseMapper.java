package com.wxm.mapper;

import com.wxm.model.OAEnterprise;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OAEnterpriseMapper {
    int deleteByPrimaryKey(Integer enterpriseId);

    int insert(OAEnterprise record);

    int insertSelective(OAEnterprise record);

    OAEnterprise selectByPrimaryKey(Integer enterpriseId);

    int updateByPrimaryKeySelective(OAEnterprise record);

    int updateByPrimaryKey(OAEnterprise record);

    int count(@Param("name") String name);

    List<OAEnterprise> list(@Param("name") String name, @Param("offset") Integer offset, @Param("limit") Integer limit);
    List<OAEnterprise> total();
}