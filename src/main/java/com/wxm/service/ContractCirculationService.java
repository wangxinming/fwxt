package com.wxm.service;

import com.wxm.entity.ReportItem;
import com.wxm.model.OAContractCirculation;
import com.wxm.model.OAContractCirculationWithBLOBs;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public interface ContractCirculationService {
    OAContractCirculationWithBLOBs querybyId(int id);
    OAContractCirculationWithBLOBs selectByProcessInstanceId(String processInstanceId);
    Integer insert(OAContractCirculationWithBLOBs oaContractTemplate);
    Integer update(OAContractCirculationWithBLOBs oaContractTemplate);
    Integer delete(int id);
    List<ReportItem> count(Date startTime, Date endTime);
    Integer total(String contractStatus,String contractType,Date startTime, Date endTime);
    Integer groupCount(Date startTime, Date endTime);
    List<ReportItem> group(Date startTime, Date endTime, Integer offset,  Integer limit);
}
