package com.wxm.mapper;

import com.wxm.model.OAAudit;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface OAAuditMapper {

    int deleteByPrimaryKey(Integer auditId);

    int insert(OAAudit record);

    int insertSelective(OAAudit record);

    OAAudit selectByPrimaryKey(Integer auditId);

    List<OAAudit> list(@Param("offset") Integer offset, @Param("limit") Integer limit,
                       @Param("userName") String userName, @Param("startTime") Date startTime,@Param("endTime") Date endTime);

    int count(@Param("userName") String userName, @Param("startTime") Date startTime,@Param("endTime") Date endTime);

    int updateByPrimaryKeySelective(OAAudit record);

    int updateByPrimaryKey(OAAudit record);
}