package com.wxm.service;

import com.wxm.model.OAContractCirculation;
import com.wxm.model.OAContractCirculationWithBLOBs;

public interface ContractCirculationService {
    OAContractCirculationWithBLOBs querybyId(int id);
    OAContractCirculationWithBLOBs selectByProcessInstanceId(String processInstanceId);
    Integer insert(OAContractCirculationWithBLOBs oaContractTemplate);
    Integer update(OAContractCirculationWithBLOBs oaContractTemplate);
    Integer delete(int id);
}
