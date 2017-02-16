package com.sohu.sns.monitor.common.services;

import com.sohu.sns.monitor.common.dao.httpResource.HttpResourceDAO;
import com.sohu.sns.monitor.common.module.HttpResource;
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
        return httpResourceDAO.getResources();
    }

    public List<HttpResource> getAllResources() {
        return httpResourceDAO.getAllResources();
    }

    public void updateResource(HttpResource httpResource ){
        httpResourceDAO.updateResource(httpResource);
    }

    public void  deleteResource(HttpResource httpResource){
        httpResourceDAO.deleteResource(httpResource);
    }

    public  void createResource(HttpResource httpResource){
        httpResourceDAO.createResource(httpResource);
    }


}
