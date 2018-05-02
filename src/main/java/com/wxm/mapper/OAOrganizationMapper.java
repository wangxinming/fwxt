package com.wxm.mapper;

import com.wxm.model.OAOrganization;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface OAOrganizationMapper {
    int deleteByPrimaryKey(Integer organizationId);

    int insert(OAOrganization record);

    int insertSelective(OAOrganization record);

    OAOrganization selectByPrimaryKey(Integer organizationId);

    int updateByPrimaryKeySelective(OAOrganization record);

    int updateByPrimaryKey(OAOrganization record);

    List<OAOrganization> list(@Param("offset") Integer offset, @Param("limit") Integer limit,
                              @Param("organizationName") String userName);
    int count(@Param("organizationName") String userName);

    List<OAOrganization> getOrganizition();
}