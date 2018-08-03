package com.wxm.service;

import com.wxm.entity.ReportItem;
import com.wxm.model.OAContractCirculation;
import com.wxm.model.OAContractCirculationWithBLOBs;
import org.omg.PortableInterceptor.INACTIVE;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public interface ContractCirculationService {
    OAContractCirculationWithBLOBs querybyId(int id);
    OAContractCirculation queryBasebyId(int id);
    OAContractCirculationWithBLOBs selectByProcessInstanceId(String processInstanceId);
    OAContractCirculation selectBaseByProcessInstanceId(String processInstanceId);

    OAContractCirculation selectByMaxId();
    Integer insert(OAContractCirculationWithBLOBs oaContractTemplate);
    Integer update(OAContractCirculationWithBLOBs oaContractTemplate);
    Integer delete(int id);
    List<ReportItem> count(Date startTime, Date endTime);
    Integer total(String contractStatus,String contractType,Date startTime, Date endTime);
    Integer groupCount(Date startTime, Date endTime);
    List<ReportItem> group(Date startTime, Date endTime, Integer offset,  Integer limit);
    Integer groupUserReport(Date startTime, Date endTime, String contractStatus, Integer userId, Integer templateId, Integer contractReopen);
    List<ReportItem> groupEnterpriseReport(Date startTime, Date endTime, String contractStatus, Integer enterpriseId, Integer templateId, Integer contractReopen);

}
