package com.wxm.service.impl;

import com.wxm.entity.ReportItem;
import com.wxm.mapper.OAContractCirculationMapper;
import com.wxm.model.OAContractCirculation;
import com.wxm.model.OAContractCirculationWithBLOBs;
import com.wxm.service.ContractCirculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class ContractCirculationServiceImpl implements ContractCirculationService {
    @Autowired
    private OAContractCirculationMapper oaContractCirculationMapper;

    @Override
    public OAContractCirculationWithBLOBs querybyId(int id) {
        return oaContractCirculationMapper.selectByPrimaryKey(id);
    }

    @Override
    public OAContractCirculationWithBLOBs selectByProcessInstanceId(String processInstanceId) {
        return oaContractCirculationMapper.selectByProcessInstanceId(processInstanceId);
    }

    @Override
    public Integer insert(OAContractCirculationWithBLOBs oaContractTemplate) {
        return oaContractCirculationMapper.insertSelective(oaContractTemplate);
    }

    @Override
    public Integer update(OAContractCirculationWithBLOBs oaContractTemplate) {
        return oaContractCirculationMapper.updateByPrimaryKeySelective(oaContractTemplate);
    }

    @Override
    public Integer delete(int id) {
        return oaContractCirculationMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<ReportItem> count(Date startTime, Date endTime) {
        return oaContractCirculationMapper.count(startTime,endTime);
    }

    @Override
    public Integer total(String contractStatus,String contractType,Date startTime, Date endTime) {
        return oaContractCirculationMapper.total(contractStatus,contractType,startTime,endTime);
    }

    @Override
    public Integer groupCount(Date startTime, Date endTime) {
        return oaContractCirculationMapper.groupCount(startTime,endTime);
    }

    @Override
    public List<ReportItem> group(Date startTime, Date endTime,Integer offset,Integer limit) {
        return oaContractCirculationMapper.group(startTime,endTime,offset,limit);
    }
}
