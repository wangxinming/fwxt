package com.wxm.service;

import com.wxm.model.OAContractTemplate;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ConcactTemplateService {
    OAContractTemplate querybyId(int id);
    Integer insert(OAContractTemplate oaContractTemplate);
    Integer update(OAContractTemplate oaContractTemplate);
    Integer delete(int id);
    List<OAContractTemplate> listTemplate();
    List<OAContractTemplate> list(@Param("offset") Integer offset, @Param("limit") Integer limit);
    Integer count();
}
