package com.wxm.service.impl;

import com.wxm.mapper.OADeploymentTemplateRelationMapper;
import com.wxm.model.OADeploymentTemplateRelation;
import com.wxm.service.OADeploymentTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OADeploymentTemplateServiceImpl implements OADeploymentTemplateService {
    @Autowired
    private OADeploymentTemplateRelationMapper oaDeploymentTemplateRelationMapper;

    @Override
    public OADeploymentTemplateRelation querybyId(int id) {
        return oaDeploymentTemplateRelationMapper.selectByPrimaryKey(id);
    }

    @Override
    public Integer insert(OADeploymentTemplateRelation oaContractTemplate) {
        return oaDeploymentTemplateRelationMapper.insertSelective(oaContractTemplate);
    }

    @Override
    public Integer update(OADeploymentTemplateRelation oaContractTemplate) {
        return oaDeploymentTemplateRelationMapper.updateByPrimaryKeySelective(oaContractTemplate);
    }

    @Override
    public Integer delete(int id) {
        return oaDeploymentTemplateRelationMapper.deleteByPrimaryKey(id);
    }

    @Override
    public Integer coutRelByDeploymentId(String deploymentId) {
        return oaDeploymentTemplateRelationMapper.coutRelByDeploymentId(deploymentId);
    }

    @Override
    public OADeploymentTemplateRelation selectByDeploymentId(String deployment) {
        return oaDeploymentTemplateRelationMapper.selectByDeploymentId(deployment);
    }
}
