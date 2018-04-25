package com.wxm;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class MailTest {
    @Autowired
    private JavaMailSender mailSender;

    @Test
    public void sendSimpleMail() throws Exception {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("dyc87112@qq.com");
        message.setTo("dyc87112@qq.com");
        message.setSubject("主题：简单邮件");
        message.setText("测试邮件内容");

        mailSender.send(message);
    }

    @Test
    public void stringBuilderTest(){
        StringBuilder sb = new StringBuilder("<input name=\"name_1324324\">");
        int size = sb.indexOf("name_1324324");
        size +="name_1324324".length();
        sb.insert(size+1," value=\"hello\"");
    }

}
