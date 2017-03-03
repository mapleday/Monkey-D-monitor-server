package com.sohu.sns.monitor.web.service.PersonDutyService;

import com.sohu.sns.monitor.common.module.NotifyPerson;
import com.sohu.sns.monitor.common.services.NotifyService;
import com.sohu.sns.monitor.common.utils.NotifyUtils;
import com.sohu.sns.monitor.web.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.sohu.snscommon.utils.service.SignatureUtil.AppKey.request;

/**
 * Created by yw on 2017.3.1
 */
@Component
public class SelectPersonDutyService {

    //    @Scheduled(cron = "0 0/5 * * * ? ")
    private static List<NotifyPerson> notifyPeoples;
    private static Integer flag;
    private static String sendMsgServer = "";


    private static NotifyUtils notifyUtils = new NotifyUtils();
    private static NotifyService notifyService;
    private static Map<String, NotifyPerson> notifyNamesMap = new HashMap<String, NotifyPerson>();
    private static List<NotifyPerson> configureNotifyPersons = new ArrayList<NotifyPerson>();



    @Autowired
    public void setNotifyService(NotifyService notifyService) {
        SelectPersonDutyService.notifyService = notifyService;
    }

    @Scheduled(cron = "0 0/1 20 * * ?")
    public static void sendDutyInfo() {
        /**发送值班提醒邮件和短信**/
        int flag=0;
        notifyPeoples = notifyService.getDutyPerson();
        while((notifyPeoples.isEmpty())&&flag!=3){
                notifyService.initDutyInGroup();
                notifyPeoples = notifyService.getDutyPerson();
                flag++;
        }
        if (flag==3){
            System.out.println("没有人值班！");
            return;
        }
        StringBuffer dutyNames=new StringBuffer("[");
        for (NotifyPerson np:notifyPeoples)
            dutyNames.append(np.getName()+",");
        dutyNames.setCharAt(dutyNames.length()-1,']');
        NotifyPerson dutyPerson = notifyPeoples.get(0);
        List<String> receiveMsgPhones=notifyService.getReceiveMsgPhones();
        String msg = "[Test]你好，当前值班通知是：今天是%s值班" + DateUtils.getCurrentTime() + ",值班顺序是  :" + dutyNames.toString();
        msg=String.format(msg,dutyPerson.getName());
//        for (String phone:receiveMsgPhones)
//             notifyUtils.sendWeixin(phone,msg);
        notifyUtils.sendWeixin("13051807977", msg);
        notifyService.removeInDutyGroup(dutyPerson.getId());
        System.out.println(msg);

        }
    }
