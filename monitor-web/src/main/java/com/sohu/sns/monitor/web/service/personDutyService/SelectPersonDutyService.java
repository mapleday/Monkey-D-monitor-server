package com.sohu.sns.monitor.web.service.personDutyService;

import com.sohu.sns.monitor.common.module.NotifyPerson;
import com.sohu.sns.monitor.common.services.NotifyService;
import com.sohu.sns.monitor.common.utils.NotifyUtils;
import com.sohu.snscommon.utils.DateUtilTool;
import com.sohu.snscommon.utils.LOGGER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by yw on 2017.3.1
 */
@Component
public class SelectPersonDutyService {
    @Autowired
    private  NotifyService notifyService;
    private  String currentTime;

    @Scheduled(cron = "0 0/1 18 * * ?")
    public void sendDutyInfo() {
        /**发送值班微信提醒**/
        //总计需要值班人
        currentTime=DateUtilTool.format(new Date(),"yyyy-MM-dd HH:mm:ss");
        List<NotifyPerson> dutyPersons = notifyService.getDutyPersons();
        //还需要发送消息的人,还需要值班的人
        List<NotifyPerson> waitForDutyPersons = notifyService.getWaitForDutyPersons();
        if (dutyPersons.isEmpty()) {
            LOGGER.appErroLog("没有人值班！");
            return;
        }
        //正常发送值班消息
        if (!waitForDutyPersons.isEmpty()) {
            nomalSendDutyMsg(dutyPersons,waitForDutyPersons);
        }
        //处理待值班人小于等于1时，重新初始化值班顺序
        if (waitForDutyPersons.size() <= 1) {
            initDutyNum(dutyPersons,waitForDutyPersons);
        }
    }

    public  void  nomalSendDutyMsg(List<NotifyPerson> dutyPersons,List<NotifyPerson> waitForDutyPersons){
        NotifyPerson dutyPerson = waitForDutyPersons.get(0);
        List<String> sendMsgPersonNames = new ArrayList<String>();
        sendMsgPersonNames.addAll(notifyService.getHasSendMsgPersonNames());
        for (NotifyPerson np : waitForDutyPersons) {
            sendMsgPersonNames.add(np.getName());
        }
        String msg = "你好，当前值班通知是：今天是%s值班" + currentTime + ",值班顺序是  :" + sendMsgPersonNames.toString();
        msg = String.format(msg, dutyPerson.getName());
        int dutyGroupNum = -dutyPerson.getDutyIngroup();
        dutyPerson.setDutyIngroup(dutyGroupNum);
        notifyService.updateDutyGroupNum(dutyPerson);
        System.out.println(msg);
//        sendWeixinNotify(dutyPersons,msg);
    }

    public  void  initDutyNum(List<NotifyPerson> dutyPersons,List<NotifyPerson> waitForDutyPersons){
        int dutyNum = 1;
        Collections.shuffle(dutyPersons);
        for (NotifyPerson np : dutyPersons) {
            np.setDutyIngroup(dutyNum++);
            notifyService.updateDutyGroupNum(np);
        }
        if (waitForDutyPersons.size() == 1) {
            List<String> nextRoundDutyNames = new ArrayList<String>();
            for (NotifyPerson np : dutyPersons) {
                nextRoundDutyNames.add(np.getName());
            }
            String msg = "你好，当前一轮值班已完成" + currentTime + ",下一轮值班顺序是  :" + nextRoundDutyNames.toString();
//            sendWeixinNotify(dutyPersons,msg);
            System.out.println(msg);
        } else
            sendDutyInfo();
    }

    public  void sendWeixinNotify(List<NotifyPerson> dutyPersons, String msg) {
        for (NotifyPerson dutyPerson : dutyPersons) {
            NotifyUtils.sendWeixin(dutyPerson.getPhone(), msg);
        }
    }
}