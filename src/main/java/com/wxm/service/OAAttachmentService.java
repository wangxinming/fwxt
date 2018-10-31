package com.wxm.service;

import com.wxm.model.OAAttachment;

import java.util.List;

public interface OAAttachmentService {
    Integer save(OAAttachment oaAttachment);
    Integer update(OAAttachment oaAttachment);
    Integer updateByProcessId(String processIdBefore, String processIdAfter );
    Integer delete(Integer id);
    Integer deleteByName(String name);
    Integer deleteByProcessId(String processId);
    List<OAAttachment> list(Integer contractId);
    OAAttachment get(Integer id);
    OAAttachment getByFileName(String fileName);
    List<OAAttachment> listByProcessId(String  processId);
    List<OAAttachment> listBlobByProcessId(String  processId);

}
