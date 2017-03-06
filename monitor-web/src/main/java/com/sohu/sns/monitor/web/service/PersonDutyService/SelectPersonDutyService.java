package com.sohu.sns.monitor.web.service.PersonDutyService;

import com.sohu.sns.monitor.common.module.NotifyPerson;
import com.sohu.sns.monitor.common.services.NotifyService;
import com.sohu.sns.monitor.common.utils.NotifyUtils;
import com.sohu.sns.monitor.web.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.*;

/**
 * Created by yw on 2017.3.1
 */
@Component
public class SelectPersonDutyService {

    private static NotifyUtils notifyUtils = new NotifyUtils();
    private static NotifyService notifyService;
    @Autowired
    public void setNotifyService(NotifyService notifyService) {
        SelectPersonDutyService.notifyService = notifyService;
    }

    @Scheduled(cron = "0 0/1 9 * * ?")
    public static void sendDutyInfo() {
        /**发送值班微信提醒**/
        int dutyNum = 1;
        List<NotifyPerson> dutyPersons = notifyService.getDutyPersons();//总计需要值班人
        List<NotifyPerson> waitForDutyPersons = notifyService.getWaitForDutyPersons();//还需要发送消息的人,还需要值班的人
        if (dutyPersons.isEmpty()) {
            System.out.println("没有人要值班！");
            return;
        }
        if (!waitForDutyPersons.isEmpty()) {//当前不为空则发送消息
            NotifyPerson dutyPerson = waitForDutyPersons.get(0);
            List<String> sendMsgPersonNames = new ArrayList<String>();//一轮值班中需要发送消息人的名字
            sendMsgPersonNames.addAll(notifyService.getHasSendMsgPersonNames());
            for (NotifyPerson np : waitForDutyPersons)
                sendMsgPersonNames.add(np.getName());
            String msg = "[Test]你好，当前值班通知是：今天是%s值班" + DateUtils.getCurrentTime() + ",值班顺序是  :" + sendMsgPersonNames.toString();
            msg = String.format(msg, dutyPerson.getName());
            int dutyGroupNum = dutyPerson.getDutyIngroup();
            dutyGroupNum = (-dutyGroupNum);
            System.out.println(dutyGroupNum + "num----");
            dutyPerson.setDutyIngroup(dutyGroupNum);
            notifyService.setDutyGroupNum(dutyPerson);
            System.out.println(msg);
//            sendWeixinNotify(dutyPersons,msg);
            NotifyUtils.sendWeixin("13051807977",msg);

        }//进行初始化消息队列
        if (waitForDutyPersons.size() <= 1) {//包含通过改数据库后size 为0的状态
            Collections.shuffle(dutyPersons);
            for (NotifyPerson np : dutyPersons) {
                np.setDutyIngroup(dutyNum++);
                notifyService.setDutyGroupNum(np);
            }
            if (waitForDutyPersons.size() == 1) {
                List<String> nextRoundDutyNames = new ArrayList<String>();
                for (NotifyPerson np : dutyPersons) {
                    nextRoundDutyNames.add(np.getName());
                }
                String msg = "[Test]你好，当前一轮值班已完成" + DateUtils.getCurrentTime() + ",下一轮值班顺序是  :" + nextRoundDutyNames.toString();
                System.out.println(msg);
//                发送微信通知
//                sendWeixinNotify(dutyPersons,msg);
//                只发给自己
                NotifyUtils.sendWeixin("13051807977",msg);
            } else
                sendDutyInfo();
        }
    }
    public static void sendWeixinNotify(List<NotifyPerson> dutyPersons,String msg){
        for (NotifyPerson dutyPerson:dutyPersons){
            NotifyUtils.sendWeixin(dutyPerson.getPhone(),msg);
        }
    }
    }