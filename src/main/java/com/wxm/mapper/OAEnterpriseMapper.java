package com.wxm.mapper;

import com.wxm.model.OAEnterprise;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OAEnterpriseMapper {
    int deleteByPrimaryKey(Integer enterpriseId);

    int insert(OAEnterprise record);

    int insertSelective(OAEnterprise record);

    OAEnterprise selectByPrimaryKey(Integer enterpriseId);

    int updateByPrimaryKeySelective(OAEnterprise record);

    int updateByPrimaryKey(OAEnterprise record);

    int count(@Param("name") String name);

    List<OAEnterprise> list(@Param("name") String name, @Param("offset") Integer offset, @Param("limit") Integer limit);
    List<OAEnterprise> total();
    List<OAEnterprise> getEnterpriseByLevel(Integer level);
    List<OAEnterprise> getEnterpriseByParentId(Integer id);
    List<OAEnterprise> getEnterpriseByLoction();
    List<OAEnterprise> getEnterpriseByProvince(@Param("location") String location);
    List<OAEnterprise> getEnterpriseByCity(@Param("location")String location,@Param("province")String province);
    List<OAEnterprise> groupByName();
    List<OAEnterprise> listByName(@Param("name") String name);

    List<OAEnterprise> listEnterprise(@Param("location")String location,@Param("province")String province,@Param("city")String city);

}