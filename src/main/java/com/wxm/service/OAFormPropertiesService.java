package com.wxm.service;

import com.wxm.model.OAFormProperties;

import java.util.List;

public interface OAFormPropertiesService {
    OAFormProperties querybyId(int id);
    Integer insert(OAFormProperties oaContractTemplate);
    Integer update(OAFormProperties oaContractTemplate);
    Integer delete(int id);
    Integer deleteByTemplateId(int templateId);
    List<OAFormProperties> listByTemplateId(int templateId);
    List<OAFormProperties> list(String templateName);
    Integer count(String templateName);
}
