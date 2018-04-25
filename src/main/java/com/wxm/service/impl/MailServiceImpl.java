package com.wxm.service.impl;

import com.wxm.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {

    @Autowired
    private JavaMailSender mailSender;
    @Value("${spring.mail.enable}")
    private boolean mailEnable;
    @Override
    public boolean send(SimpleMailMessage message) {
        boolean ret = false;
        try {
            if (mailEnable) {
                mailSender.send(message);
                ret = true;
            }
        }catch (Exception e){
            ret = false;
        }
        return ret;
    }
}
