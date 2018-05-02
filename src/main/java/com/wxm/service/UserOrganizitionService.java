package com.wxm.service;

import com.wxm.model.OAOrganization;

import java.util.List;
import java.util.Map;

public interface UserOrganizitionService {
    boolean save(OAOrganization oaOrganization);
    boolean update(OAOrganization oaOrganization);
    boolean delete(OAOrganization oaOrganization);
    List<OAOrganization> getOrganizition();
    Map getGroupList(Integer offset,Integer limit);

}
