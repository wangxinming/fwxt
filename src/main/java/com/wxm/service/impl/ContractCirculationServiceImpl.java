package com.wxm.service.impl;

import com.wxm.mapper.OAContractCirculationMapper;
import com.wxm.model.OAContractCirculation;
import com.wxm.model.OAContractCirculationWithBLOBs;
import com.wxm.service.ContractCirculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
