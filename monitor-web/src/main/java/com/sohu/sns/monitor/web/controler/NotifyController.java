package com.sohu.sns.monitor.web.controler;

import com.sohu.sns.monitor.common.module.NotifyPerson;
import com.sohu.sns.monitor.common.services.NotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yw on 2017/2/9.
 */
@Controller
public class NotifyController {
    @Autowired
    NotifyService notifyService;

    @RequestMapping("/notifyPerson")
    public String notifyPerson(){
        return "notifyPerson";
    }

    @ResponseBody
    @RequestMapping(value = "/getAllPerson")
    public Map getAllPerson(){
        HashMap<String,Object> map=new HashMap<String,Object>();
        map.put("data",notifyService.getAllPerson());
        map.put("options","");
        map.put("files","");
        return map;

    }

    @ResponseBody
    @RequestMapping(value = "/updatePerson",method = RequestMethod.POST)
    public Map updatePerson(NotifyPerson np){
        int originDutyStatus=notifyService.getDutyStatus(np.getId());
        if (np.getWaitDutyStatus()==1&&originDutyStatus==0){
            int dutyNum=notifyService.getMaxDutyGroupNum()+1;
            np.setDutyIngroup(dutyNum);
        }
        notifyService.updatePerson(np);
        Map<String,Object> map=new HashMap<String,Object>();
        ArrayList<NotifyPerson> list=new ArrayList<NotifyPerson>();
        list.add(np);
        map.put("data",list);
        return map;
    }

    @ResponseBody
    @RequestMapping(value = "/deletePerson",method = RequestMethod.POST)
    public NotifyPerson  deletePerson(NotifyPerson np){
        notifyService.deletePerson(np);
        return new NotifyPerson();
    }

    @ResponseBody
    @RequestMapping(value = "/createPerson",method = RequestMethod.POST)
    public  Map createPerson( NotifyPerson notifyPerson){
        int dutyNum=notifyService.getMaxDutyGroupNum()+1;
        notifyPerson.setDutyIngroup(dutyNum);
        notifyService.createPerson(notifyPerson);
        Map<String,Object> map=new HashMap<String,Object>();
        ArrayList<NotifyPerson> list=new ArrayList<NotifyPerson>();
        list.add(notifyPerson);
        map.put("data",list);
        return map;
    }

}
