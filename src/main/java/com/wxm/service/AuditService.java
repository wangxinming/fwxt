package com.wxm.service;


import com.wxm.model.OAAudit;

import java.util.Date;
import java.util.List;

public interface AuditService {
     boolean audit(OAAudit info);
//     List<OAAudit> list(int offset, int limit, String userName, Date startTime, Date endTime);
//     Integer count(String userName, Date startTime, Date endTime);
     Object getAudits(int offset, int limit, String userName, Date startTime, Date endTime);
}
