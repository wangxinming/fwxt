package com.wxm.service.impl;

import com.wxm.mapper.OAPositionRelationMapper;
import com.wxm.model.OAPositionRelation;
import com.wxm.service.OAPositionRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class OAPositionRelationServiceImpl implements OAPositionRelationService {
    @Autowired
    private OAPositionRelationMapper oaPositionRelationMapper;
    @Override
    public List<OAPositionRelation> list(Integer offset, Integer limit) {
        return oaPositionRelationMapper.list(offset,limit);
    }

    @Override
    public OAPositionRelation getById(Integer id) {
        return oaPositionRelationMapper.selectByPrimaryKey(id);
    }

    @Override
    public Integer count() {
        return oaPositionRelationMapper.count();
    }

    @Override
    public Integer create(OAPositionRelation oaPositionRelation) {
        return oaPositionRelationMapper.insertSelective(oaPositionRelation);
    }

    @Override
    public Integer update(OAPositionRelation oaPositionRelation) {
        return oaPositionRelationMapper.updateByPrimaryKeySelective(oaPositionRelation);
    }

    @Override
    public List<OAPositionRelation> getByCompanyPosition(String company, String position) {
        return oaPositionRelationMapper.queryByCompanyPosition(company,position);
    }

    @Override
    public Integer delete(Integer id) {
        return oaPositionRelationMapper.deleteByPrimaryKey(id);
    }
}
