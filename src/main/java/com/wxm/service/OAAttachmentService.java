package com.wxm.service;

import com.wxm.model.OAAttachment;

import java.util.List;

public interface OAAttachmentService {
    Integer save(OAAttachment oaAttachment);
    Integer update(OAAttachment oaAttachment);
    Integer delete(Integer id);
    List<OAAttachment> list(Integer contractId);
    OAAttachment get(Integer id);

}
