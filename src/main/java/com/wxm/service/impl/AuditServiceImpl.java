package com.wxm.service.impl;

import com.wxm.mapper.OAAuditMapper;
import com.wxm.model.OAAudit;
import com.wxm.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class AuditServiceImpl implements AuditService {
    @Autowired
    private OAAuditMapper oaAuditMapper;
    @Override
    public boolean audit(OAAudit info) {
        try{
            oaAuditMapper.insertSelective(info);
            if(info.getAuditId()>0)
                return true;
        }catch (Exception e){

        }
        return false;
    }

    @Override
    public List<OAAudit> list(int offset, int limit, String userName, Date startTime, Date endTime) {
        return oaAuditMapper.list(offset,limit,userName,startTime,endTime);
    }

    @Override
    public Integer count(String userName, Date startTime, Date endTime) {
        return oaAuditMapper.count(userName,startTime,endTime);
    }
}
