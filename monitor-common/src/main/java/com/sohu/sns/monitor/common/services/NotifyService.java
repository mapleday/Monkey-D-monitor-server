package com.sohu.sns.monitor.common.services;

import com.sohu.sns.monitor.common.module.NotifyPerson;
import com.sohu.sns.monitor.common.dao.notifyPerson.NotifyPersonDao;
import com.sohu.sns.monitor.common.utils.NotifyUtils;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * author:jy
 * time:16-12-28下午3:33
 * 通知服务类
 */
@Service
public class NotifyService {
    @Autowired
    NotifyPersonDao notifyPersonDao;

    /**
     * 发送消息给所有人
     *
     * @param message 　消息
     */
    public void sendAllNotifyPerson(String message) {
        List<NotifyPerson> allPerson = notifyPersonDao.getAllPerson();
        if (allPerson == null || allPerson.isEmpty()) {
            LOGGER.buziLog(ModuleEnum.MONITOR_SERVICE, "NotifyService.sendAllNotifyPerson", message, "no person");
            return;
        }

        for (NotifyPerson notifyPerson : allPerson) {
            NotifyUtils.sendAlert(notifyPerson.getPhone(), message);
        }
    }

    /**
     * update by yw on 2017.2.9
     */
    public List<NotifyPerson> getAllPerson(){
        return notifyPersonDao.getAllPerson();
    }

    public List<NotifyPerson> getDutyPerson(){
        return notifyPersonDao.getDutyPerson();
    }

    public void creatPerson(NotifyPerson notifyPerson){
        notifyPersonDao.createPerson(notifyPerson);
    }

    public void updatePerson(NotifyPerson notifyPerson){
        notifyPersonDao.updatePerson(notifyPerson);
    }

    public void deletePerson(NotifyPerson notifyPerson){
        notifyPersonDao.deletePerson(notifyPerson);
    }



}
