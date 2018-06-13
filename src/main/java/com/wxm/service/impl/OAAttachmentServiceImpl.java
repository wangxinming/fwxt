package com.wxm.service.impl;

import com.wxm.mapper.OAAttachmentMapper;
import com.wxm.model.OAAttachment;
import com.wxm.service.OAAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OAAttachmentServiceImpl implements OAAttachmentService {
    @Autowired
    private OAAttachmentMapper oaAttachmentMapper;
    @Override
    public Integer save(OAAttachment oaAttachment) {
         oaAttachmentMapper.insertSelective(oaAttachment);
        return oaAttachment.getAttachmentId();
    }

    @Override
    public Integer update(OAAttachment oaAttachment) {
        return oaAttachmentMapper.updateByPrimaryKeySelective(oaAttachment);
    }

    @Override
    public Integer delete(Integer id) {
        return oaAttachmentMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<OAAttachment> list(Integer contractId) {
        return oaAttachmentMapper.list(contractId);
    }

    @Override
    public OAAttachment get(Integer id) {
        return oaAttachmentMapper.selectByPrimaryKey(id);
    }
}
