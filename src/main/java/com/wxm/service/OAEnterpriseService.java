package com.wxm.service;

import com.wxm.model.OAEnterprise;

import java.util.List;
import java.util.Map;

public interface OAEnterpriseService {
    List<OAEnterprise> getEnterpriseList(String name,Integer offset, Integer limit);
    List<OAEnterprise> getEnterpriseByLevel(Integer level);
    List<OAEnterprise> getEnterpriseByParentId(Integer id);
    List<OAEnterprise> total();
    OAEnterprise getEnterpriseById(Integer id);
    OAEnterprise getEnterpriseByName(String name);
    Integer count(String name);
    Integer create(OAEnterprise oaEnterprise);
    Integer update(OAEnterprise oaEnterprise);
    List<OAEnterprise> groupByName();
    List<OAEnterprise> listByName(String name);
    List<OAEnterprise> getEnterpriseByLoction();
    List<OAEnterprise> getEnterpriseByProvince(String location);
    List<OAEnterprise> getEnterpriseByCity(String location,String province);
    List<OAEnterprise> listEnterprise(String location,String province,String city);


}
