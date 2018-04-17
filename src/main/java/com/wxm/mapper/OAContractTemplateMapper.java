package com.wxm.mapper;

import com.wxm.model.OAContractTemplate;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OAContractTemplateMapper {
    int deleteByPrimaryKey(Integer templateId);

    int insert(OAContractTemplate record);

    int insertSelective(OAContractTemplate record);

    OAContractTemplate selectByPrimaryKey(Integer templateId);

    int updateByPrimaryKeySelective(OAContractTemplate record);

    int updateByPrimaryKeyWithBLOBs(OAContractTemplate record);

    int updateByPrimaryKey(OAContractTemplate record);

    List<OAContractTemplate> listTemplate();

    List<OAContractTemplate> list(@Param("offset") Integer offset, @Param("limit") Integer limit);

    int count();
}