package com.wxm.service;

import com.wxm.model.OAEnterprise;

import java.util.List;
import java.util.Map;

public interface OAEnterpriseService {
    List<OAEnterprise> getEnterpriseList(String name,Integer offset, Integer limit);
    List<OAEnterprise> total();
    OAEnterprise getEnterpriseById(Integer id);
    Integer count(String name);
    Integer create(OAEnterprise oaEnterprise);
    Integer update(OAEnterprise oaEnterprise);
}
