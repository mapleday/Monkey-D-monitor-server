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
 * Created by Gary on 2015/12/24.
 */
@Component
public class SelectPersonDutyService {

    //    @Scheduled(cron = "0 0/5 * * * ? ")
    private static List<NotifyPerson> notifyPeople;
    private static  Integer flag;

    private  static NotifyUtils notifyUtils=new NotifyUtils();
    private  static NotifyService  notifyService;


    @Autowired
    public  void setNotifyService(NotifyService notifyService){
        SelectPersonDutyService.notifyService=notifyService;
    }

    @Scheduled(cron = "0 0 18 * * ?")
    public  static void sendDutyInfo()  {
            /**发送值班提醒邮件和短信**/
        notifyPeople=notifyService.getDutyPerson();
//        if(null==flag)
//            flag=new Random().nextInt(notifyPeople.size()-1);
        if(null==flag||flag==notifyPeople.size()){
            flag=0;
            Collections.shuffle(notifyPeople);
        }
        List<String> list=new ArrayList<String>();
        for (NotifyPerson np:notifyPeople){
            list.add(np.getName());
        }
        System.out.println("值班顺序： "+list);
        NotifyPerson dutyPerson=notifyPeople.get(flag++);
        String msg="[Test]你好，当前值班通知是：今天是%s值班"+ DateUtils.getCurrentDate()+",值班顺序是  :"+list;
//        if("袁巍".equals(dutyPerson.getName()))
//             notifyUtils.sendWeixin(dutyPerson.getPhone(),String.format(msg,dutyPerson.getName()));
//        else
//            notifyUtils.sendWeixin("13051807977",String.format(msg,dutyPerson.getName()));
        for (NotifyPerson np:notifyPeople){
            notifyUtils.sendWeixin(np.getPhone(),String.format(msg,dutyPerson.getName()));
        }








//            System.out.println("值班人：" + personInfo.getName() + "time : " + DateUtils.getCurrentTime());
    }
}