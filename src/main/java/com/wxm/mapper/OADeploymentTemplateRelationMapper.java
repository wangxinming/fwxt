package com.wxm.mapper;

import com.wxm.model.OADeploymentTemplateRelation;

public interface OADeploymentTemplateRelationMapper {
    int deleteByPrimaryKey(Integer relationId);

    int insert(OADeploymentTemplateRelation record);

    int insertSelective(OADeploymentTemplateRelation record);

    OADeploymentTemplateRelation selectByPrimaryKey(Integer relationId);

    OADeploymentTemplateRelation selectByDeploymentId(String deployment);

    int updateByPrimaryKeySelective(OADeploymentTemplateRelation record);

    int updateByPrimaryKey(OADeploymentTemplateRelation record);

    int coutRelByDeploymentId(String deploymentId);
}