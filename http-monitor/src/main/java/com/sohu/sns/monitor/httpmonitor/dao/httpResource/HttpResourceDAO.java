package com.sohu.sns.monitor.httpmonitor.dao.httpResource;

import com.sohu.sns.monitor.httpmonitor.model.HttpResource;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * author:jy
 * time:16-10-14上午11:41
 * 资源配置类
 */
@Repository
public interface HttpResourceDAO {
    public List<HttpResource> getResources();
}
