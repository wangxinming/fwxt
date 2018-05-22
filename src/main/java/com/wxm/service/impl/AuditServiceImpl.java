package com.wxm.service.impl;

import com.wxm.mapper.OAAuditMapper;
import com.wxm.model.OAAudit;
import com.wxm.service.AuditService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuditServiceImpl implements AuditService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditServiceImpl.class);
    @Autowired
    private OAAuditMapper oaAuditMapper;
    @Override
    public boolean audit(OAAudit info) {
        try{
            oaAuditMapper.insertSelective(info);
            if(info.getAuditId()>0)
                return true;
        }catch (Exception e){
            LOGGER.error("异常",e);
        }
        return false;
    }

//    @Override
//    public List<OAAudit> list(int offset, int limit, String userName, Date startTime, Date endTime) {
//        return oaAuditMapper.list(offset,limit,userName,startTime,endTime);
//    }
//
//    @Override
//    public Integer count(String userName, Date startTime, Date endTime) {
//        return oaAuditMapper.count(userName,startTime,endTime);
//    }

    @Override
    public Object getAudits(int offset, int limit, String userName, Date startTime, Date endTime) {
        Map<String, Object> result = new HashMap<>();
        result.put("result","success");
        try {
            if(StringUtils.isBlank(userName)){
                userName = null;
            }
            result.put("rows",oaAuditMapper.list(offset,limit,userName,startTime,endTime));
            result.put("total",oaAuditMapper.count(userName,endTime,endTime));
        }catch (Exception e){
            result.put("result","failed");
            LOGGER.error("异常",e);
        }
        return result;
    }
}
