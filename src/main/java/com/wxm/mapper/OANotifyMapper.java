package com.wxm.mapper;

import com.wxm.model.OANotify;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface OANotifyMapper {
    int deleteByPrimaryKey(Integer notifyId);

    int insert(OANotify record);

    int insertSelective(OANotify record);

    OANotify selectByPrimaryKey(Integer notifyId);

    int updateByPrimaryKeySelective(OANotify record);

    int updateByPrimaryKey(OANotify record);

    List<OANotify> list(@Param("offset") Integer offset, @Param("limit") Integer limit,
                        @Param("userName") String userName, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    Integer count(@Param("userName") String userName, @Param("startTime") Date startTime,@Param("endTime") Date endTime);
}