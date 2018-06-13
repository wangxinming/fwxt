package com.wxm.mapper;

import com.wxm.model.OAFormProperties;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OAFormPropertiesMapper {
    int deleteByPrimaryKey(Integer propertiesId);

    int deleteByTemplateId(Integer templateId);

    int insert(OAFormProperties record);

    int insertSelective(OAFormProperties record);

    OAFormProperties selectByPrimaryKey(Integer propertiesId);

    int updateByPrimaryKeySelective(OAFormProperties record);

    int updateByPrimaryKey(OAFormProperties record);

    List<OAFormProperties> listByTemplateId(Integer templateId);

    List<OAFormProperties> list(@Param("offset") Integer offset, @Param("limit") Integer limit, @Param("templateName") String templateName, @Param("templateId") Integer templateId);

    int count(@Param("templateName") String templateName, @Param("templateId") Integer templateId);
}