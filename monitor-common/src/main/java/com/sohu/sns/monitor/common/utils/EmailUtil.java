package com.sohu.sns.monitor.common.utils;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Created by Gary on 2015/10/20.
 */
public class EmailUtil {

    private static String MAIL_FROM = "postmaster@sns.com";
    private static final String HOST = "10.11.156.63";
    private static final String PASSWD = "xxxxxxx";

    /**
     * 发送简单文本邮件
     * @param subject  邮件主题
     * @param content   邮件内容
     * @param to   发送到的邮箱地址，可发送多个
     */
    public static void sendSimpleEmail(String subject, String content, String... to) throws Exception {

        JavaMailSenderImpl sender = new JavaMailSenderImpl();

        //设置邮箱服务器
        sender.setHost(HOST);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom(MAIL_FROM);
        message.setSubject(subject);
        message.setText(content);

        sender.setUsername(MAIL_FROM);
        sender.setPassword(PASSWD);

        Properties prop  =  new Properties() ;
        prop.put("mail.smtp.auth", "true") ;  //  将这个参数设为true，让服务器进行认证,认证用户名和密码是否正确
        prop.put("mail.smtp.timeout", "25000") ;
        sender.setJavaMailProperties(prop);

        sender.send(message);
    }

    /**
     * 发送HTML邮件
     * @param subject  邮件主题
     * @param content   邮件内容
     * @param to   发送到的邮箱地址，可发送多个
     */
    public static void sendHtmlEmail(String subject, String content, String... to) throws Exception {

        JavaMailSenderImpl sender = new JavaMailSenderImpl();

        //设置邮箱服务器
        sender.setHost(HOST);

        MimeMessage mailMessage = sender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage,true,"GBK");;
        messageHelper.setTo(to);
        messageHelper.setFrom(MAIL_FROM);
        messageHelper.setSubject(subject);
        messageHelper.setText(content, true);

        sender.setUsername(MAIL_FROM);
        sender.setPassword(PASSWD);

        Properties prop  =  new Properties() ;
        prop.put("mail.smtp.auth", "true") ;  //  将这个参数设为true，让服务器进行认证,认证用户名和密码是否正确
        prop.put("mail.smtp.timeout", "25000") ;
        sender.setJavaMailProperties(prop);

        sender.send(mailMessage);
    }


}
