package com.sohu.sns.monitor.web.controler;

import com.sohu.sns.monitor.common.module.NotifyPerson;
import com.sohu.sns.monitor.common.services.NotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/1/11.
 */

@Controller
public class NotifyControler {

    @Autowired
    NotifyService notifyService;

    @RequestMapping("/NotifyPerson")
    public String index(Model model) {
        return "notifyperson";
    }

    @RequestMapping(value = "/updateNotifyPerson")
    @ResponseBody
    public int updateNotifyPerson(NotifyPerson notifyPerson) {
        return notifyService.updateNotifyPerson(notifyPerson);
    }

    @RequestMapping(value = "/addNotifyPerson")
    @ResponseBody
    public int addNotifyPerson(NotifyPerson notifyPerson) {
        return notifyService.addNotifyPerson(notifyPerson);
    }


    @RequestMapping(value = "/deleteNotifyPerson")
    @ResponseBody
    public int deleteNotifyPerson(NotifyPerson notifyPerson) {
        return notifyService.addNotifyPerson(notifyPerson);
    }


    @RequestMapping(value = "/getNotifyPersonList")
    @ResponseBody
    public Object getNotifyPersonList(NotifyPerson notifyPerson) {
        Map<String,Object> notifyPersonListListMap = new HashMap<String,Object>();
        List<NotifyPerson> notifyPersonListList = notifyService.getAllPerson();
        notifyPersonListListMap.put("data",notifyPersonListList);
        return notifyPersonListListMap;
    }


}
