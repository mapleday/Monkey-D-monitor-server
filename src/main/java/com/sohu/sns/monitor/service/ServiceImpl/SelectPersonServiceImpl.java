package com.sohu.sns.monitor.service.ServiceImpl;

import com.sohu.sns.monitor.model.PersonInfo;
import com.sohu.sns.monitor.service.SelectPersonService;
import com.sohu.sns.monitor.util.DateUtil;
import com.sohu.snscommon.utils.EmailUtil;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.SMS;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.http.HttpClientUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by Gary on 2015/12/24.
 */
@Component
public class SelectPersonServiceImpl implements SelectPersonService {

    private static final String SMS_EMAIL_URL = "http://sns-mail-sms.apps.sohuno.com";
    private static final String PERSON_PHONE_DEV = "18910556026";
    private static final String PERSON_EMAIL_DEV = "shouqinchen@sohu-inc.com";

    private int flag = 0;

    @Value("#{myProperties[on_duty_person]}")
    private String person_config;

    @Value("#{myProperties[duty_msg]}")
    private String dutyMsg;

    @Value("#{myProperties[duty_mail_subject]}")
    private String dutyMailSubject;

    private List<PersonInfo> personInfos = null;


    @Override
    public void send() throws Exception {
        try {

            initEnv();  //解析值班人信息
            PersonInfo personInfo = personInfos.get(flag++);
            /**发送值班提醒邮件和短信**/
            String dutyContent = String.format(dutyMsg, personInfo.getName());
            dutyMailSubject = String.format(dutyMailSubject, DateUtil.getCurrentDate());
            StringBuilder emailSb = new StringBuilder();
            StringBuilder smsSb = new StringBuilder();
            for(PersonInfo p : personInfos) {
                if(0 != emailSb.length()) {
                    emailSb.append("|");
                }
                if(0 != smsSb.length()) {
                    smsSb.append(",");
                }
                emailSb.append(p.getEmail());
                smsSb.append(p.getPhone());
            }
            Map<String, String> map = new HashMap<String, String>();
            map.put("subject", dutyMailSubject);
            map.put("text", dutyContent);
            map.put("to", emailSb.toString());
            try {
                //String result = new HttpClientUtil().postByUtf(SMS_EMAIL_URL + "/sendSimpleEmail", map, null);
                //if(!"success".equals(result)) {
                    EmailUtil.sendSimpleEmail("值班邮件发送失败提醒", "值班邮件发送失败，今天的值班人是"+personInfo.getName()+"，请及时提醒", PERSON_EMAIL_DEV);
                    SMS.sendMessage(PERSON_PHONE_DEV, "当天值班人邮件信息发送失败，值班人："+personInfo.getName()+"，请及时提醒");
                //}
            } catch (Exception e) {
                LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "select_person.sendEmail", null, null, e);
                EmailUtil.sendSimpleEmail("值班邮件发送失败提醒", "值班邮件发送失败，今天的值班人是" + personInfo.getName() + "，请及时提醒", PERSON_EMAIL_DEV);
                SMS.sendMessage(PERSON_PHONE_DEV, "当天值班人邮件信息发送失败，值班人："+personInfo.getName()+"，请及时提醒");
            } finally {
                map.clear();
            }
            map.put("phoneNo", smsSb.toString());
            map.put("msg", dutyContent);
            try {
                String result = new HttpClientUtil().getByUtf(SMS_EMAIL_URL+"/sendSms", map);
                if(!"success".equals(result)) {
                    EmailUtil.sendSimpleEmail("值班短信发送失败提醒", "值班短信发送失败，今天的值班人是" + personInfo.getName() + "，请及时提醒", PERSON_EMAIL_DEV);
                    SMS.sendMessage(PERSON_PHONE_DEV, "当天值班人短信信息发送失败，值班人：" + personInfo.getName() + "，请及时提醒");
                }
            } catch (Exception e) {
                LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "select_person.sendSms", null, null, e);
                EmailUtil.sendSimpleEmail("值班短信发送失败提醒", "值班短信发送失败，今天的值班人是" + personInfo.getName() + "，请及时提醒", PERSON_EMAIL_DEV);
                SMS.sendMessage(PERSON_PHONE_DEV, "当天值班人短信信息发送失败，值班人："+personInfo.getName()+"，请及时提醒");
            } finally {
                map.clear();
            }
            System.out.println("值班人：" + personInfo.getName() + "time : " + DateUtil.getCurrentTime());
            if(flag == personInfos.size()) {
                flag = 0;
                Collections.shuffle(personInfos);
            }
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "select_person", null, null, e);
            e.printStackTrace();
        }
    }

    /**
     * 初始化值班人员信息
     */
    private void initEnv() {
        if(null == personInfos) {
            String[] arr = person_config.split("\\|");
            personInfos = new ArrayList<PersonInfo>();
            for(int i=0; i<arr.length; i++) {
                String[] everyPerson = arr[i].split(",");
                if(3 == everyPerson.length) {
                    personInfos.add(new PersonInfo(everyPerson[0], everyPerson[1], everyPerson[2]));
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new SelectPersonServiceImpl().send();
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

    public String getDutyMailSubject() {
        return dutyMailSubject;
    }

    public void setDutyMailSubject(String dutyMailSubject) {
        this.dutyMailSubject = dutyMailSubject;
    }
}
