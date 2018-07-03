package com.wxm.service;

import com.wxm.model.OANotify;

import java.util.Date;
import java.util.List;

public interface OANotifyService {
        List<OANotify> list(String userName, Integer offset, Integer limit,Date startTime,Date endTime);
        OANotify getNotifyById(Integer id);
        Integer count(String userName, Date startTime, Date endTime);
        Integer create(OANotify oaNotify);
        Integer update(OANotify oaNotify);
        Integer delete(Integer id);
}
