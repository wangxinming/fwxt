package com.wxm.service;

import org.springframework.mail.SimpleMailMessage;

public interface MailService {
    boolean send(SimpleMailMessage message);
}
