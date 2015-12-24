package com.sohu.sns.monitor.service.ServiceImpl;

import com.sohu.sns.monitor.model.PersonInfo;
import com.sohu.sns.monitor.service.SelectPersonService;
import com.sohu.snscommon.utils.http.HttpClientUtil;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by Gary on 2015/12/24.
 */
@Component
public class SelectPersonServiceImpl implements SelectPersonService {

    private static final String BASE_URL = "http://sns-mail-sms.apps.sohuno.com";
    private static final String CONTENT = "友情提示你，今天是%s值班";
    private static final String PRIVATE_CONTENT = "亲爱的%s，今天你值班，请不要忘记";

    List<PersonInfo> personInfos = Arrays.asList(
        new PersonInfo("王国青", "guoqingwang@sohu-inc.com", "18511871276"),
        new PersonInfo("杨明光", "morganyang@sohu-inc.com", "18513387052"),
        new PersonInfo("时尽营", "jinyingshi@sohu-inc.com", "18910556026"),
        new PersonInfo("杨守松", "shousongyang@sohu-inc.com", "18600405242"),
        new PersonInfo("陈守钦", "shouqinchen@sohu-inc.com", "13121556477")
    );

    @Override
    public void send(String total) throws Exception {
        int max = Integer.parseInt(total);
        int random = new Random().nextInt(max);

        int id = random % personInfos.size();

        PersonInfo personInfo = personInfos.get(id);

        Map<String, String> privateEmailMap = new HashMap<String, String>();
        privateEmailMap.put("subject", "今日值班提醒");
        privateEmailMap.put("text", String.format(PRIVATE_CONTENT, personInfo.getName()));
        privateEmailMap.put("to", personInfo.getEmail());
        new HttpClientUtil().getByUtf(BASE_URL + "/sendSimpleEmail", privateEmailMap);

        Map<String, String> privateSmsMap = new HashMap<String, String>();
        privateSmsMap.put("phoneNo", personInfo.getPhone());
        privateSmsMap.put("msg", String.format(PRIVATE_CONTENT, personInfo.getName()));
        new HttpClientUtil().getByUtf(BASE_URL + "/sendSms", privateSmsMap);

        StringBuilder smsSb = new StringBuilder();
        StringBuilder emailSb = new StringBuilder();

        for(PersonInfo p : personInfos) {
            if(personInfo.getName().equals(p.getName())) {
                continue;
            }
            if(smsSb.length() != 0) {
                smsSb.append(",");
            }
            smsSb.append(p.getPhone());

            if(emailSb.length() != 0) {
                emailSb.append("|");
            }
            emailSb.append(p.getEmail());
        }

        Map<String, String> publicEmailMap = new HashMap<String, String>();
        publicEmailMap.put("subject", "值班人通知");
        publicEmailMap.put("text", String.format(CONTENT, personInfo.getName()));
        publicEmailMap.put("to", emailSb.toString());
        new HttpClientUtil().getByUtf(BASE_URL + "/sendSimpleEmail", publicEmailMap);

        Map<String, String> publicSmsMap = new HashMap<String, String>();
        publicSmsMap.put("phoneNo", smsSb.toString());
        publicSmsMap.put("msg", String.format(CONTENT, personInfo.getName()));
        new HttpClientUtil().getByUtf(BASE_URL + "/sendSms", publicSmsMap);
    }
}
