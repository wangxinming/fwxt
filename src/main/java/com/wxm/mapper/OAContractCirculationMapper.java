package com.wxm.mapper;

import com.wxm.entity.ReportItem;
import com.wxm.model.OAContractCirculation;
import com.wxm.model.OAContractCirculationWithBLOBs;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public interface OAContractCirculationMapper {
    int deleteByPrimaryKey(Integer contractId);

    int insert(OAContractCirculationWithBLOBs record);

    int insertSelective(OAContractCirculationWithBLOBs record);

    OAContractCirculationWithBLOBs selectByPrimaryKey(Integer contractId);

    OAContractCirculationWithBLOBs selectByProcessInstanceId(String processInstanceId);

    int updateByPrimaryKeySelective(OAContractCirculationWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(OAContractCirculationWithBLOBs record);

    int updateByPrimaryKey(OAContractCirculation record);
    //统计模板类合同，各自数量
    List<ReportItem> count(@Param("startTime") Date startTime, @Param("endTime") Date endTime);
    //统计总数量
    int total(@Param("contractStatus")String contractStatus,@Param("contractType")String contractType,@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    //统计模板类合同，各自数量
    List<ReportItem> group(@Param("startTime") Date startTime, @Param("endTime") Date endTime,@Param("offset") Integer offset, @Param("limit") Integer limit);

    Integer groupCount(@Param("startTime") Date startTime, @Param("endTime") Date endTime);
}