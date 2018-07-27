package com.wxm.service;

import com.wxm.model.OAPositionRelation;

import java.util.Date;
import java.util.List;

public interface OAPositionRelationService {
    List<OAPositionRelation> list(Integer offset, Integer limit);
    OAPositionRelation getById(Integer id);
    Integer count();
    Integer create(OAPositionRelation oaPositionRelation);
    Integer update(OAPositionRelation oaPositionRelation);
    List<OAPositionRelation> getByCompanyPosition(String company,String position);
    Integer delete(Integer id);
}
