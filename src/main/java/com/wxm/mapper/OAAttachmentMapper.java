package com.wxm.mapper;

import com.wxm.model.OAAttachment;

import java.util.List;

public interface OAAttachmentMapper {
    int deleteByPrimaryKey(Integer attachmentId);

    int insert(OAAttachment record);

    int insertSelective(OAAttachment record);

    OAAttachment selectByPrimaryKey(Integer attachmentId);

    int updateByPrimaryKeySelective(OAAttachment record);

    int updateByPrimaryKeyWithBLOBs(OAAttachment record);

    int updateByPrimaryKey(OAAttachment record);

    List<OAAttachment> list(Integer contractId);
}