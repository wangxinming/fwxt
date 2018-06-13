package com.wxm.service;

import com.wxm.model.OAFormProperties;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FormPropertiesService {
    OAFormProperties querybyId(int id);
    Integer insert(OAFormProperties oaContractTemplate);
    Integer update(OAFormProperties oaContractTemplate);
    Integer delete(int id);
    Integer deleteByTemplateId(int templateId);
    List<OAFormProperties> listByTemplateId(Integer templateId);
    List<OAFormProperties> list(@Param("offset") Integer offset, @Param("limit") Integer limit, @Param("templateName" ) String templateName, @Param("templateId" ) Integer templateId);
    Integer count(@Param("templateName") String templateName, @Param("templateId" ) Integer templateId);

//    List<OAFormProperties> list(String templateName);
//    Integer count(String templateName);
}
