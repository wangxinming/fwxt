package com.wxm.mapper;

import com.wxm.model.OAContractCirculation;
import com.wxm.model.OAContractCirculationWithBLOBs;

public interface OAContractCirculationMapper {
    int deleteByPrimaryKey(Integer contractId);

    int insert(OAContractCirculationWithBLOBs record);

    int insertSelective(OAContractCirculationWithBLOBs record);

    OAContractCirculationWithBLOBs selectByPrimaryKey(Integer contractId);

    OAContractCirculationWithBLOBs selectByProcessInstanceId(String processInstanceId);

    int updateByPrimaryKeySelective(OAContractCirculationWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(OAContractCirculationWithBLOBs record);

    int updateByPrimaryKey(OAContractCirculation record);
}