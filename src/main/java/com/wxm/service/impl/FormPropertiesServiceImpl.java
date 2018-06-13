package com.wxm.service.impl;

import com.wxm.mapper.OAFormPropertiesMapper;
import com.wxm.model.OAContractTemplate;
import com.wxm.model.OAFormProperties;
import com.wxm.service.FormPropertiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FormPropertiesServiceImpl implements FormPropertiesService {
    @Autowired
    private OAFormPropertiesMapper oaFormPropertiesMapper;
    @Override
    public OAFormProperties querybyId(int id) {
        return oaFormPropertiesMapper.selectByPrimaryKey(id);
    }

    @Override
    public Integer insert(OAFormProperties oaFormProperties) {
        return oaFormPropertiesMapper.insertSelective(oaFormProperties);
    }

    @Override
    public Integer update(OAFormProperties oaFormProperties) {
        return oaFormPropertiesMapper.updateByPrimaryKeySelective(oaFormProperties);
    }

    @Override
    public Integer delete(int id) {
        return oaFormPropertiesMapper.deleteByPrimaryKey(id);
    }

    @Override
    public Integer deleteByTemplateId(int templateId) {
        return oaFormPropertiesMapper.deleteByTemplateId(templateId);
    }

    @Override
    public List<OAFormProperties> listByTemplateId(Integer templateId) {
        return oaFormPropertiesMapper.listByTemplateId(templateId);
    }

    @Override
    public List<OAFormProperties> list(Integer offset, Integer limit, String templateName,Integer templateId ) {
        return oaFormPropertiesMapper.list(offset,limit,templateName,templateId);
    }

    @Override
    public Integer count(String templateName,Integer templateId) {
        return oaFormPropertiesMapper.count(templateName,templateId);
    }
}
