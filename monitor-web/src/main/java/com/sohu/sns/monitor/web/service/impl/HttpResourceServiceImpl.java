package com.sohu.sns.monitor.web.service.impl;

import com.sohu.sns.monitor.web.dao.HttpResourceDAO;
import com.sohu.sns.monitor.web.domain.HttpResource;
import com.sohu.sns.monitor.web.service.HttpResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by yzh on 2016/11/22.
 */
@Service
public class HttpResourceServiceImpl implements HttpResourceService {
    @Autowired
    HttpResourceDAO httpResourceDAO;

    @Override
    public List<HttpResource> getResources() {
        System.out.println("xx");
        return httpResourceDAO.getResources();
    }
}
