package com.wxm.service.impl;

import com.wxm.mapper.OANotifyMapper;
import com.wxm.model.OANotify;
import com.wxm.service.OANotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class OANotifyServiceImpl implements OANotifyService {
    @Autowired
    private OANotifyMapper oaNotifyMapper;
    @Override
    public List<OANotify> list(String userName, Integer offset, Integer limit, Date startTime, Date endTime) {
        return oaNotifyMapper.list(offset,limit,userName,startTime,endTime);
    }

    @Override
    public OANotify getNotifyById(Integer id) {
        return oaNotifyMapper.selectByPrimaryKey(id);
    }

    @Override
    public Integer count(String userName, Date startTime, Date endTime) {
        return oaNotifyMapper.count(userName,startTime,endTime);
    }

    @Override
    public Integer create(OANotify oaNotify) {
        return oaNotifyMapper.insertSelective(oaNotify);
    }

    @Override
    public Integer update(OANotify oaNotify) {
        return oaNotifyMapper.updateByPrimaryKeySelective(oaNotify);
    }

    @Override
    public Integer delete(Integer id) {
        return oaNotifyMapper.deleteByPrimaryKey(id);
    }
}
