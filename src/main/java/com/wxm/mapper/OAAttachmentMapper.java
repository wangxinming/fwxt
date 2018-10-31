package com.wxm.mapper;

import com.wxm.model.OAAttachment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OAAttachmentMapper {
    int deleteByPrimaryKey(Integer attachmentId);

    int deleteByName(@Param("fileName") String fileName);
    int deleteByProcessId(@Param("processId") String processId);

    int insert(OAAttachment record);

    int insertSelective(OAAttachment record);

    OAAttachment selectByPrimaryKey(Integer attachmentId);

    int updateByPrimaryKeySelective(OAAttachment record);

    int updateByProcessId(@Param("processIdBefore") String processIdBefore,@Param("processIdAfter") String processIdAfter);

    int updateByPrimaryKeyWithBLOBs(OAAttachment record);

    int updateByPrimaryKey(OAAttachment record);

    List<OAAttachment> list(Integer contractId);

    OAAttachment getByFileName(@Param("fileName") String fileName);

    List<OAAttachment> listByProcessId( @Param("processId") String processId);
    List<OAAttachment> listBlobByProcessId( @Param("processId") String processId);


}