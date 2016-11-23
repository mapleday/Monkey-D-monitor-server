package com.sohu.sns.monitor.web.service;

import com.sohu.sns.monitor.web.domain.HttpResource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by yzh on 2016/11/22.
 */
public interface HttpResourceService {

    public List<HttpResource> getResources();
}
