package com.sohu.sns.monitor.common.services;

import com.sohu.sns.monitor.common.dao.httpResource.HttpResourceDAO;
import com.sohu.sns.monitor.common.module.HttpResource;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by yzh on 2016/11/15.
 */
@Component
public class HttpResourceService {
    @Autowired
    HttpResourceDAO httpResourceDAO;

    /**
     * 获取所有需要监控的资源
     *
     * @return
     */
    public List<HttpResource> getResources() {
//        List<HttpResource> newlist = httpResourceDAO.getResources();
//        for (int i = 0; i < newlist.size(); i++) {
//            System.out.println(newlist.get(i).toString());
//        }
        return httpResourceDAO.getResources();
    }


    /**
     * 修改资源
     */
    public int updatehttpResource(HttpResource httpResource){
        return httpResourceDAO.updateResources(httpResource);
    }

    /**
     * 增加资源
     */

    public int addhttpResource(HttpResource httpResource){
        return httpResourceDAO.addResources(httpResource);
    }

    public int deletehttpResource(HttpResource httpResource){
        return httpResourceDAO.deleteResources(httpResource);
    }

}
