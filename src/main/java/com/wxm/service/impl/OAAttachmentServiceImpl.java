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
    public Integer updateByProcessId(String processIdBefore, String processIdAfter ) {
        return oaAttachmentMapper.updateByProcessId(processIdBefore,processIdAfter);
    }

    @Override
    public Integer delete(Integer id) {
        return oaAttachmentMapper.deleteByPrimaryKey(id);
    }

    @Override
    public Integer deleteByName(String name) {
        return oaAttachmentMapper.deleteByName(name);
    }

    @Override
    public Integer deleteByProcessId(String processId) {
        return oaAttachmentMapper.deleteByProcessId(processId);
    }

    @Override
    public List<OAAttachment> list(Integer contractId) {
        return oaAttachmentMapper.list(contractId);
    }

    @Override
    public OAAttachment get(Integer id) {
        return oaAttachmentMapper.selectByPrimaryKey(id);
    }

    @Override
    public OAAttachment getByFileName(String fileName) {
        return oaAttachmentMapper.getByFileName(fileName);
    }

    @Override
    public List<OAAttachment> listByProcessId(String processId) {
        return oaAttachmentMapper.listByProcessId(processId);
    }

    @Override
    public List<OAAttachment> listBlobByProcessId(String processId) {
        return oaAttachmentMapper.listBlobByProcessId(processId);
    }
}
