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
    public OAContractCirculation queryBasebyId(int id) {
        return oaContractCirculationMapper.selectById(id);
    }

    @Override
    public OAContractCirculationWithBLOBs selectByProcessInstanceId(String processInstanceId) {
        return oaContractCirculationMapper.selectByProcessInstanceId(processInstanceId);
    }

    @Override
    public OAContractCirculation selectBaseByProcessInstanceId(String processInstanceId) {
        return oaContractCirculationMapper.selectBaseByProcessInstanceId(processInstanceId);
    }

    @Override
    public OAContractCirculation selectByMaxId() {
        return oaContractCirculationMapper.selectByMaxId();
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
    public ReportItem total(String contractStatus,Integer templateId,Integer contractReopen,String contractType,Date startTime, Date endTime) {
        return oaContractCirculationMapper.total(contractStatus,templateId,contractReopen,contractType,startTime,endTime);
    }

    @Override
    public Integer groupCount(Date startTime, Date endTime) {
        return oaContractCirculationMapper.groupCount(startTime,endTime);
    }

    @Override
    public List<ReportItem> group(Date startTime, Date endTime,Integer offset,Integer limit) {
        return oaContractCirculationMapper.group(startTime,endTime,offset,limit);
    }

    @Override
    public ReportItem groupUserReport(Date startTime, Date endTime, String contractType, String contractStatus, Integer userId, Integer templateId, Integer contractReopen) {
        return oaContractCirculationMapper.groupUserReport(startTime,endTime,contractType,contractStatus,userId,templateId,contractReopen);
    }

    @Override
    public List<ReportItem> groupEnterpriseReport(Date startTime, Date endTime, String contractType, String contractStatus, Integer enterpriseId, Integer templateId, Integer contractReopen) {
        return oaContractCirculationMapper.groupEnterpriseReport(startTime,endTime,contractType,contractStatus,enterpriseId,templateId,contractReopen);
    }

    @Override
    public List<ReportItem> groupFieldEnterpriseReport(Date startTime, Date endTime, String contractType, String field, String condition, String contractStatus, Integer enterpriseId, Integer templateId, Integer contractReopen) {
        return oaContractCirculationMapper.groupFieldEnterpriseReport(startTime,endTime,contractType,field,condition,contractStatus,enterpriseId,templateId,contractReopen);
    }

    @Override
    public List<ReportItem> groupFieldUserReport(Date startTime, Date endTime, String contractType, String field, String condition, String contractStatus, Integer enterpriseId, Integer templateId, Integer contractReopen) {
        return oaContractCirculationMapper.groupFieldUserReport(startTime,endTime,contractType,field,condition,contractStatus,enterpriseId,templateId,contractReopen);
    }
}
