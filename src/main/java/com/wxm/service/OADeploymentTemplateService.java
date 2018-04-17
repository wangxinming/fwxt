package com.wxm.service;

import com.wxm.model.OADeploymentTemplateRelation;

public interface OADeploymentTemplateService {
    OADeploymentTemplateRelation querybyId(int id);
    Integer insert(OADeploymentTemplateRelation oaContractTemplate);
    Integer update(OADeploymentTemplateRelation oaContractTemplate);
    Integer delete(int id);
    Integer coutRelByDeploymentId(String deploymentId);
    OADeploymentTemplateRelation selectByDeploymentId(String deployment);
}
