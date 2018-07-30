package com.wxm.mapper;

import com.wxm.model.OAPositionRelation;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface OAPositionRelationMapper {
    int deleteByPrimaryKey(Integer positionRelationId);

    int insert(OAPositionRelation record);

    int insertSelective(OAPositionRelation record);

    OAPositionRelation selectByPrimaryKey(Integer positionRelationId);

    int updateByPrimaryKeySelective(OAPositionRelation record);

    int updateByPrimaryKey(OAPositionRelation record);

    List<OAPositionRelation> list(@Param("offset") Integer offset, @Param("limit") Integer limit);

    Integer count();

    List<OAPositionRelation> queryByCompanyPosition(@Param("company") String company, @Param("position") String position);

}