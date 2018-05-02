package com.wxm.mapper;

import com.wxm.model.OAAuditBak;
import com.wxm.model.OAAuditBakExample;
import java.util.List;

public interface OAAuditBakMapper {
    int deleteByExample(OAAuditBakExample example);

    int deleteByPrimaryKey(Integer auditId);

    int insert(OAAuditBak record);

    int insertSelective(OAAuditBak record);

    List<OAAuditBak> selectByExample(OAAuditBakExample example);

    OAAuditBak selectByPrimaryKey(Integer auditId);

    int updateByPrimaryKeySelective(OAAuditBak record);

    int updateByPrimaryKey(OAAuditBak record);
}