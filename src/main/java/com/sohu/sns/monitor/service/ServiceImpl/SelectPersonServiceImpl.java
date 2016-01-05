package com.sohu.sns.monitor.service.ServiceImpl;

import com.sohu.sns.monitor.model.PersonInfo;
import com.sohu.sns.monitor.service.SelectPersonService;
import com.sohu.sns.monitor.util.DateUtil;
import com.sohu.snscommon.utils.EmailUtil;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.SMS;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Created by Gary on 2015/12/24.
 */
@Component
public class SelectPersonServiceImpl implements SelectPersonService {

    @Value("#{myProperties[on_duty_person]}")
    private String person_config;

    @Value("#{myProperties[duty_msg]}")
    private String dutyMsg;

    @Value("#{myProperties[un_duty_msg]}")
    private String unDutyMsg;

    @Value("#{myProperties[duty_mail_subject]}")
    private String dutyMailSubject;

    @Value("#{myProperties[un_duty_mail_subject]}")
    private String unDutyMailSubject;

    private PersonInfo[] personInfos = null;


    @Override
    public void send(String total) throws Exception {
        try {

            initEnv();
            int max = Integer.parseInt(total);
            int random = new Random(System.currentTimeMillis()).nextInt(max);

            int id = random % personInfos.length;

            PersonInfo personInfo = personInfos[id];

            /**给值班人发送提醒邮件和短信**/
            String dutyContent = String.format(dutyMsg, personInfo.getName());
            EmailUtil.sendSimpleEmail(dutyMailSubject, dutyContent, personInfo.getEmail());
            SMS.sendMessage(personInfo.getPhone(), dutyContent);

            /***给非值班人发送邮件和短信**/
            String unDutyContent = String.format(unDutyMsg, personInfo.getName());
            for(PersonInfo p : personInfos) {
                if(personInfo.getName().equals(p.getName())) {
                    continue;
                }
                EmailUtil.sendSimpleEmail(unDutyMailSubject, unDutyContent, p.getEmail());
                SMS.sendMessage(p.getPhone(), unDutyContent);
            }
            System.out.println("值班人：" + personInfo.getName() + "time : " + DateUtil.getCurrentTime());
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "select_person", total, null, e);
            e.printStackTrace();
        }
    }

    /**
     * 初始化值班人员信息
     */
    private void initEnv() {
        if(null == personInfos) {
            String[] arr = person_config.split("\\|");
            personInfos = new PersonInfo[arr.length];
            for(int i=0; i<arr.length; i++) {
                String[] everyPerson = arr[i].split(",");
                if(3 == everyPerson.length) {
                    personInfos[i] = new PersonInfo(everyPerson[0], everyPerson[1], everyPerson[2]);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new SelectPersonServiceImpl().send("10000");
    }

    public String getPerson_config() {
        return person_config;
    }

    public void setPerson_config(String person_config) {
        this.person_config = person_config;
    }

    public String getDutyMsg() {
        return dutyMsg;
    }

    public void setDutyMsg(String dutyMsg) {
        this.dutyMsg = dutyMsg;
    }

    public String getUnDutyMsg() {
        return unDutyMsg;
    }

    public void setUnDutyMsg(String unDutyMsg) {
        this.unDutyMsg = unDutyMsg;
    }

    public String getDutyMailSubject() {
        return dutyMailSubject;
    }

    public void setDutyMailSubject(String dutyMailSubject) {
        this.dutyMailSubject = dutyMailSubject;
    }

    public String getUnDutyMailSubject() {
        return unDutyMailSubject;
    }

    public void setUnDutyMailSubject(String unDutyMailSubject) {
        this.unDutyMailSubject = unDutyMailSubject;
    }
}
