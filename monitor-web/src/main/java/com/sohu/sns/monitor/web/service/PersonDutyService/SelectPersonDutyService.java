package com.sohu.sns.monitor.web.service.PersonDutyService;

import com.sohu.sns.monitor.common.module.NotifyPerson;
import com.sohu.sns.monitor.common.services.NotifyService;
import com.sohu.sns.monitor.common.utils.NotifyUtils;
import com.sohu.sns.monitor.web.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import scala.util.parsing.combinator.testing.Str;

import java.util.*;

/**
 * Created by yw on 2017.3.1
 */
@Component
public class SelectPersonDutyService {

    //    @Scheduled(cron = "0 0/5 * * * ? ")
    private static List<NotifyPerson> notifyPeople;
    private static  Integer flag;

    private  static NotifyUtils notifyUtils=new NotifyUtils();
    private  static NotifyService  notifyService;
    private  static Map<String,NotifyPerson> notifyNameMap=new HashMap<String, NotifyPerson>();
    public   static List<NotifyPerson> configureNotifyPerson=new ArrayList<NotifyPerson>();



    @Autowired
    public  void setNotifyService(NotifyService notifyService){
        SelectPersonDutyService.notifyService=notifyService;
    }

    @Scheduled(cron = "0 0 19 * * ?")
    public  static void sendDutyInfo()  {
            /**发送值班提醒邮件和短信**/

//        if(null==flag)
//            flag=new Random().nextInt(notifyPeople.size()-1);
        if(null==flag||flag==notifyPeople.size()){
            notifyPeople=notifyService.getDutyPerson();
            flag=0;
            notifyNameMap.clear();
            Collections.shuffle(notifyPeople);
            for (NotifyPerson np:notifyPeople){
                notifyNameMap.put(np.getName(),np);
            }
        }
//        System.out.println("值班顺序： "+list);
//        存在bug ，修改值班状态存在延时！
        if (!configureNotifyPerson.isEmpty()){
                for (NotifyPerson np:configureNotifyPerson){
                    if (!notifyNameMap.containsKey(np.getName())){
                        notifyPeople.add(np);
                        notifyNameMap.put(np.getName(),np);
                    }
                    else if (np.getWaitDutyStatus()==0) {
                        notifyPeople.remove(notifyNameMap.get(np.getName()));
                        notifyNameMap.remove(np.getName());
                    }
                }
            configureNotifyPerson.clear();
        }

        if(flag>=notifyPeople.size()){
            Collections.shuffle(notifyPeople);
            flag=0;
        }

        NotifyPerson dutyPerson=notifyPeople.get(flag++);
        List<String>  nameList=new ArrayList<String>();
        for (NotifyPerson np:notifyPeople)
            nameList.add(np.getName());
        String msg="[Test]你好，当前值班通知是：今天是%s值班"+ DateUtils.getCurrentTime()+",值班顺序是  :"+nameList.toString();
        for (NotifyPerson np:notifyPeople){
            notifyUtils.sendWeixin(np.getPhone(),String.format(msg,dutyPerson.getName()));
        }
        msg=String.format(msg,dutyPerson.getName());
        notifyUtils.sendWeixin("13051807977",msg);
        System.out.println(msg);
    }
}