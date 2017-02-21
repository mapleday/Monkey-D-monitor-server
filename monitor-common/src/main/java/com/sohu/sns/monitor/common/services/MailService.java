package com.sohu.sns.monitor.common.services;

import com.sohu.sns.monitor.common.dao.notifyPerson.NotifyPersonDao;
import com.sohu.sns.monitor.common.module.NotifyPerson;
import com.sohu.sns.monitor.common.utils.NotifyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by morgan on 2017/2/21.
 * 处理发送报警邮件的服务
 */
@Service
public class MailService {

    @Autowired
    NotifyPersonDao notifyPersonDao;

    public void sendMailToGroup(String groupName, String htmlContent) {

        List<NotifyPerson> groupPerson = notifyPersonDao.getGroupPerson(groupName);
        for (NotifyPerson notifyPerson : groupPerson) {
            NotifyUtils.sendMail(notifyPerson.getEmail(), "PASSPORT接口监控", htmlContent);
        }

    }

}
