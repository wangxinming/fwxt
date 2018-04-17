package com.wxm.service.impl;

import com.wxm.mapper.OAContractTemplateMapper;
import com.wxm.model.OAContractTemplate;
import com.wxm.service.ConcactTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContractTemplateServiceImpl implements ConcactTemplateService {

    @Autowired
    private OAContractTemplateMapper oaContractTemplateMapper;

    @Override
    public OAContractTemplate querybyId(int id) {
        return oaContractTemplateMapper.selectByPrimaryKey(id);
    }

    @Override
    public Integer insert(OAContractTemplate oaContractTemplate) {
        return oaContractTemplateMapper.insertSelective(oaContractTemplate);
    }

    @Override
    public Integer update(OAContractTemplate oaContractTemplate) {
        return oaContractTemplateMapper.updateByPrimaryKeySelective(oaContractTemplate);
    }

    @Override
    public Integer delete(int id) {
        return oaContractTemplateMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<OAContractTemplate> listTemplate() {
        return oaContractTemplateMapper.listTemplate();
    }

    @Override
    public List<OAContractTemplate> list(Integer offset, Integer limit) {
        return oaContractTemplateMapper.list(offset,limit);
    }

    @Override
    public Integer count() {
        return oaContractTemplateMapper.count();
    }
}
