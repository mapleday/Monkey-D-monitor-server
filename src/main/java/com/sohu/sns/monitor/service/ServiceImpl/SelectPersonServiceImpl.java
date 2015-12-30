package com.sohu.sns.monitor.service.ServiceImpl;

import com.sohu.sns.monitor.model.PersonInfo;
import com.sohu.sns.monitor.service.SelectPersonService;
import com.sohu.snscommon.utils.EmailUtil;
import com.sohu.snscommon.utils.SMS;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by Gary on 2015/12/24.
 */
@Component
public class SelectPersonServiceImpl implements SelectPersonService {

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
        int random = new Random(System.currentTimeMillis()).nextInt(max);

        int id = random % personInfos.size();

        PersonInfo personInfo = personInfos.get(id);

        EmailUtil.sendSimpleEmail("今日值班提醒", String.format(PRIVATE_CONTENT, personInfo.getName()), personInfo.getEmail());
        SMS.sendMessage(personInfo.getPhone(), String.format(PRIVATE_CONTENT, personInfo.getName()));

        for(PersonInfo p : personInfos) {
            if(personInfo.getName().equals(p.getName())) {
                continue;
            }
            EmailUtil.sendSimpleEmail("今日值班人通知", String.format(CONTENT, personInfo.getName()), p.getEmail());
            SMS.sendMessage(p.getPhone(), String.format(CONTENT, personInfo.getName()));
        }

    }

    public static void main(String[] args) throws Exception {
        new SelectPersonServiceImpl().send("10000");
    }
}
