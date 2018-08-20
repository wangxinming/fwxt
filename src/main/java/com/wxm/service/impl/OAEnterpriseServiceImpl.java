package com.wxm.service.impl;

import com.wxm.mapper.OAEnterpriseMapper;
import com.wxm.model.OAEnterprise;
import com.wxm.service.OAEnterpriseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OAEnterpriseServiceImpl implements OAEnterpriseService {
    @Autowired
    private OAEnterpriseMapper oaEnterpriseMapper;


    @Override
    public List<OAEnterprise> getEnterpriseList(String name,Integer offset, Integer limit) {
        return oaEnterpriseMapper.list(name,offset,limit);
    }

    @Override
    public List<OAEnterprise> getEnterpriseByLevel (Integer level) {
        return oaEnterpriseMapper.getEnterpriseByLevel(level);
    }

    @Override
    public List<OAEnterprise> getEnterpriseByParentId(Integer id) {
        return oaEnterpriseMapper.getEnterpriseByParentId(id);
    }

    @Override
    public List<OAEnterprise> total() {
        return oaEnterpriseMapper.total();
    }

    @Override
    public OAEnterprise getEnterpriseById(Integer id) {
        return oaEnterpriseMapper.selectByPrimaryKey(id);
    }


    @Override
    public Integer count(String name) {
        return oaEnterpriseMapper.count(name);
    }

    @Override
    public Integer create(OAEnterprise oaEnterprise) {
        return oaEnterpriseMapper.insertSelective(oaEnterprise);
    }

    @Override
    public Integer update(OAEnterprise oaEnterprise) {
        return oaEnterpriseMapper.updateByPrimaryKeySelective(oaEnterprise);
    }

    @Override
    public List<OAEnterprise> groupByName() {
        return oaEnterpriseMapper.groupByName();
    }

    @Override
    public List<OAEnterprise> listByName(String name) {
        return oaEnterpriseMapper.listByName(name);
    }

    @Override
    public List<OAEnterprise> getEnterpriseByLoction() {
        return oaEnterpriseMapper.getEnterpriseByLoction();
    }

    @Override
    public List<OAEnterprise> getEnterpriseByProvince(String location) {
        return oaEnterpriseMapper.getEnterpriseByProvince(location);
    }

    @Override
    public List<OAEnterprise> getEnterpriseByCity(String location, String province) {
        return oaEnterpriseMapper.getEnterpriseByCity(location,province);
    }

    @Override
    public List<OAEnterprise> listEnterprise(String location, String province, String city) {
        return oaEnterpriseMapper.listEnterprise(location,province,city);
    }
}
