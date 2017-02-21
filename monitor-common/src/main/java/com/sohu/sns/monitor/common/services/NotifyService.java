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

    public void sendNotifyToPersonGroup(String message, String groupName) {
        List<NotifyPerson> groupPerson = notifyPersonDao.getGroupPerson(groupName);
        if (groupPerson == null || groupPerson.isEmpty()) {
            LOGGER.buziLog(ModuleEnum.MONITOR_SERVICE, "NotifyService.sendAllNotifyPerson", message, "no person");
            return;
        }
        for (NotifyPerson notifyPerson : groupPerson) {
            NotifyUtils.sendAlert(notifyPerson.getPhone(), message);
        }
    }


    public List<NotifyPerson> getAllPerson(){
        return notifyPersonDao.getAllPerson();
    }

    /**
     * 添加记录
     * @return
     */
    public int addNotifyPerson(NotifyPerson notifyPerson){
        return notifyPersonDao.addNotifyPerson(notifyPerson);
    }

    /**
     * 修改记录
     * @param notifyPerson
     * @return
     */
    public int updateNotifyPerson(NotifyPerson notifyPerson){
        return notifyPersonDao.updateNotifyPerson(notifyPerson);
    }

    /**
     * 删除记录
     * @param notifyPerson
     * @return
     */
    public int deleteNotifyPerson(NotifyPerson notifyPerson){
        return notifyPersonDao.deleteNotifyPerson(notifyPerson);
    }

}
