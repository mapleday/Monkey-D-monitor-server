package com.sohu.sns.monitor.service.ServiceImpl;

import com.sohu.sns.common.utils.json.JsonMapper;
import com.sohu.sns.monitor.model.PersonInfo;
import com.sohu.sns.monitor.service.SelectPersonService;
import com.sohu.sns.monitor.util.DateUtil;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.SMS;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.http.HttpClientUtil;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by Gary on 2015/12/24.
 */
@Component
public class SelectPersonServiceImpl implements SelectPersonService {

    private static String sms_email_baseUrl = "";
    private static String simpleEmailInterface = "";
    private static String sendSmsInterface = "";
    private static String person_admin_phone = "";
    private static String person_admin_email = "";
    private static List<PersonInfo> dutyPersonInfos;
    private static String dutyContent = "";
    private static String dutyMailSubject = "";
    private static String failSubject = "";
    private static String failContent = "";
    private static Integer flag;

    @Override
    public void send() throws Exception {
        try {

            PersonInfo personInfo = dutyPersonInfos.get(flag++);

            /**发送值班提醒邮件和短信**/
            dutyContent = String.format(dutyContent, personInfo.getName());
            dutyMailSubject = String.format(dutyMailSubject, DateUtil.getCurrentDate());
            StringBuilder emailBuffer = new StringBuilder();
            StringBuilder smsBuffer = new StringBuilder();
            for(PersonInfo p : dutyPersonInfos) {
                if(0 != emailBuffer.length()) {
                    emailBuffer.append("|");
                }
                if(0 != smsBuffer.length()) {
                    smsBuffer.append(",");
                }
                emailBuffer.append(p.getEmail());
                smsBuffer.append(p.getPhone());
            }
            Map<String, String> map = new HashMap<String, String>();
            map.put("subject", dutyMailSubject);
            map.put("text", dutyContent);
            map.put("to", emailBuffer.toString());
            try {
                HttpClientUtil.getStringByPost(sms_email_baseUrl+simpleEmailInterface, map, null);
            } catch (Exception e) {
                SMS.sendMessage(person_admin_phone, String.format(failContent, personInfo.getName()));
            } finally {
                map.clear();
            }

            map.put("phoneNo", smsBuffer.toString());
            map.put("msg", dutyContent);
            try {
                HttpClientUtil.getStringByPost(sms_email_baseUrl+sendSmsInterface, map, null);
            } catch (Exception e) {
                SMS.sendMessage(person_admin_phone, String.format(failContent, personInfo.getName()));
            } finally {
                map.clear();
            }

            System.out.println("值班人：" + personInfo.getName() + "time : " + DateUtil.getCurrentTime());

            if(flag == dutyPersonInfos.size()) {
                flag = 0;
                Collections.shuffle(dutyPersonInfos);
            }

        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "select_person", null, null, e);
            e.printStackTrace();
        }
    }

    /**
     * 初始化值班人员信息
     */
    public static void initEnv(String monitorUrl, String dutyInfo) {
        JsonMapper jsonMapper = JsonMapper.nonDefaultMapper();
        Map<String, Object> urls = jsonMapper.fromJson(monitorUrl, HashMap.class);
        Map<String, Object> dutyInfoMap = jsonMapper.fromJson(dutyInfo, HashMap.class);
        sms_email_baseUrl = (String) urls.get("base_url");
        simpleEmailInterface = (String) urls.get("simple_email_interface");
        sendSmsInterface = (String) urls.get("send_sms_interface");
        person_admin_email = (String) dutyInfoMap.get("person_admin_email");
        person_admin_phone = (String) dutyInfoMap.get("person_admin_phone");
        dutyContent = (String) dutyInfoMap.get("duty_content");
        dutyMailSubject = (String) dutyInfoMap.get("mail_subject");
        failSubject = (String) dutyInfoMap.get("fail_subject");
        failContent = (String) dutyInfoMap.get("fail_content");
        String dutyPersonInfo = (String) dutyInfoMap.get("person_info");
        if(null != dutyPersonInfo) {
            String[] arr = dutyPersonInfo.split("\\|");
            dutyPersonInfos = new ArrayList<PersonInfo>();
            for(int i=0; i<arr.length; i++) {
                String[] everyPerson = arr[i].split(",");
                if(3 == everyPerson.length) {
                    dutyPersonInfos.add(new PersonInfo(everyPerson[0], everyPerson[1], everyPerson[2]));
                }
            }
        }
        if(null == flag) {
           flag = new Random().nextInt(dutyPersonInfos.size()-1);
        }
    }
}
