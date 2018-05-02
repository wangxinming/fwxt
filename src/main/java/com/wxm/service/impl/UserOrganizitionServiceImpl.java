package com.wxm.service.impl;

import com.wxm.mapper.OAOrganizationMapper;
import com.wxm.model.OAOrganization;
import com.wxm.service.UserOrganizitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class UserOrganizitionServiceImpl implements UserOrganizitionService{
    @Autowired
    private OAOrganizationMapper oaOrganizationMapper;
    @Override
    public boolean save(OAOrganization oaOrganization) {
        boolean ret = true;
        oaOrganizationMapper.insertSelective(oaOrganization);
        if(oaOrganization.getOrganizationId() < 1)
            ret = false;
        return ret;
    }

    @Override
    public boolean update(OAOrganization oaOrganization) {
        oaOrganizationMapper.updateByPrimaryKeySelective(oaOrganization);
        return false;
    }

    @Override
    public boolean delete(OAOrganization oaOrganization) {
        oaOrganizationMapper.deleteByPrimaryKey(oaOrganization.getOrganizationId());
        return false;
    }


    @Override
    public List<OAOrganization> getOrganizition() {
        return oaOrganizationMapper.getOrganizition();
    }

    @Override
    public Map getGroupList(Integer offset, Integer limit) {
        Map<String, Object> result = new HashMap<>();
        List<OAOrganization> list = oaOrganizationMapper.list(offset,limit,null);
        Integer count = oaOrganizationMapper.count(null);
        result.put("rows",list);
        result.put("total",count);
        return result;
    }
}
